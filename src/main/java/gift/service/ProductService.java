package gift.service;

import gift.domain.model.ProductAddRequestDto;
import gift.domain.model.ProductResponseDto;
import gift.domain.model.ProductUpdateRequestDto;
import gift.domain.repository.ProductRepository;
import gift.domain.model.Product;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품 ID입니다."));
        return convertToResponseDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> getAllProduct() {
        return productRepository.findAll().stream()
            .map(this::convertToResponseDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponseDto addProduct(@Valid ProductAddRequestDto productAddRequestDto) {
        validateProductName(productAddRequestDto.getName());
        validateDuplicateProduct(productAddRequestDto.getName());
        Product product = convertAddRequestToEntity(productAddRequestDto);
        Product savedProduct = productRepository.save(product);
        return convertToResponseDto(savedProduct);
    }

    private void validateDuplicateProductId(Long id) {
        if (productRepository.existsById(id)) {
            throw new IllegalArgumentException("이미 존재하는 상품 ID입니다.");
        }
    }

    private void validateDuplicateProduct(String name) {
        if (productRepository.existsByName(name)) {
            throw new IllegalArgumentException("이미 존재하는 상품명입니다.");
        }
    }

    private void validateProductName(String name) {
        if (name.contains("카카오")) {
            throw new IllegalArgumentException("'카카오'가 포함된 상품명은 담당 MD와 협의가 필요합니다.");
        }
    }

    @Transactional
    public ProductResponseDto updateProduct(@Valid ProductUpdateRequestDto productAddRequestDto) {
        Product product = productRepository.findById(productAddRequestDto.getId())
            .orElseThrow(() -> new NoSuchElementException("존재하지 않는 상품입니다."));
        if (productRepository.existsByName(productAddRequestDto.getName())) {
            throw new IllegalArgumentException("이미 존재하는 상품명입니다.");
        }
        product.update(productAddRequestDto.getName(), productAddRequestDto.getPrice(), productAddRequestDto.getImageUrl());
        return convertToResponseDto(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        validateExistProductId((id));
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public void validateExistProductId(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new NoSuchElementException("존재하지 않는 상품입니다.");
        }
    }

    private Product convertAddRequestToEntity(ProductAddRequestDto productAddRequestDto) {
        return new Product(
            productAddRequestDto.getName(),
            productAddRequestDto.getPrice(),
            productAddRequestDto.getImageUrl()
        );
    }

    private ProductResponseDto convertToResponseDto(Product product) {
        return new ProductResponseDto(
            product.getId(),
            product.getName(),
            product.getPrice(),
            product.getImageUrl()
        );
    }
}