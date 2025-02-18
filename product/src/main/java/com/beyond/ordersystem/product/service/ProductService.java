package com.beyond.ordersystem.product.service;

import com.beyond.ordersystem.common.service.StockInventoryService;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.repository.MemberRepository;
import com.beyond.ordersystem.product.domain.Product;
import com.beyond.ordersystem.product.dto.ProductRegisterDto;
import com.beyond.ordersystem.product.dto.ProductResDto;
import com.beyond.ordersystem.product.dto.ProductSearchDto;
import com.beyond.ordersystem.product.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;
    private final S3Client s3Client;
    private final StockInventoryService stockInventoryService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public ProductService(ProductRepository productRepository, MemberRepository memberRepository, S3Client s3Client, StockInventoryService stockInventoryService) {
        this.productRepository = productRepository;
        this.memberRepository = memberRepository;
        this.s3Client = s3Client;
        this.stockInventoryService = stockInventoryService;
    }

    public Product productCreate(ProductRegisterDto dto){
        try{
//            member 조회
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Member member = memberRepository.findByEmail(authentication.getName()).orElseThrow(()->new EntityNotFoundException("없는 회원입니다."));
            Product product = productRepository.save(dto.toEntity(member));
//            redis에 재고에 추가
            stockInventoryService.increaseStock(product.getId(), dto.getStockQuantity());
//            aws에 image 저장후에 url 추출
//        aws에 s3 접근가능한 iam계정생성, iam계정을 통해 aws에 접근 가능한 접근객체 생성
            MultipartFile image = dto.getProductImage();
            byte[] bytes = image.getBytes();
            String fileName = product.getId()+"_"+ image.getOriginalFilename();
//        먼저 local에 저장
            Path path = Paths.get("C:/Users/Playdata/Desktop/tmp/",fileName);
            Files.write(path,bytes, StandardOpenOption.CREATE,StandardOpenOption.WRITE);
//            저장 객체
            PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket(bucket).key(fileName).build();
//            저장실행
            s3Client.putObject(putObjectRequest, RequestBody.fromFile(path));
            String s3Url = s3Client.utilities().getUrl(a->a.bucket(bucket).key(fileName)).toExternalForm();
            product.updateImagePath(s3Url);
            return product;
        }catch (IOException e){
//            redis는 트랜잭션의 대상이 아니므로, 별도의 decrease작업이 필요함
            throw new RuntimeException("이미지 저장 실패");
        }
    }

    public Page<ProductResDto> findAll(Pageable pageable, ProductSearchDto searchDto){
//        검색을 위해 Specification 객체 사용
//        Specification 객체는 복잡한 쿼리를 명세를 이용하여 정의하는 방식으로 쿼리를 생성.
        Specification<Product> specification = new Specification<Product>() {
            @Override
            public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
//                root: 엔티티의 속성을 접근하기 위한 객체, criteriabuilder : 쿼리를 생성하기 위한 객체,
                List<Predicate> predicates = new ArrayList<>();
                if(searchDto.getCategory()!=null){
                    predicates.add(criteriaBuilder.equal(root.get("category"),searchDto.getCategory()));
                }if(searchDto.getProductName()!=null){
                    predicates.add(criteriaBuilder.like(root.get("name"), "%"+searchDto.getProductName()+"%"));
                }
                Predicate[] predicateArr = new Predicate[predicates.size()];
                for(int i=0; i<predicates.size();i++){
                    predicateArr[i] = predicates.get(i);
                }
                Predicate predicate = criteriaBuilder.and(predicateArr);
                return predicate;
            }
        };
        Page<Product> productList = productRepository.findAll(specification,pageable);
        return productList.map(p->p.fromEntity());
    }
}
