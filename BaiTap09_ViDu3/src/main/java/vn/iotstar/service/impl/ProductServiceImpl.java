package vn.iotstar.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import vn.iotstar.dto.ProductDTO;
import vn.iotstar.entity.Product;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.ProductMapper;
import vn.iotstar.repository.ProductRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;
import vn.iotstar.service.ProductService;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ProductMapper mapper;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public Page<ProductDTO> findAll(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                Sort.by(Sort.Direction.DESC, "id")
        );

        return productRepository
                .search(keyword == null ? "" : keyword.trim(), pageable)
                .map(mapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product không tồn tại")
                );
        return mapper.toDTO(product);
    }

    @Override
    @Transactional
    public ProductDTO create(ProductDTO dto, MultipartFile image) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User không tồn tại")
                );

        Product product = mapper.toEntity(dto);
        product.setUser(user);

        if (image != null && !image.isEmpty()) {
            CloudinaryUploadResult result = cloudinaryService.upload(image);
            product.setImageUrl(result.url() + "|" + result.publicId());
        }

        return mapper.toDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO update(Long id, ProductDTO dto, MultipartFile image) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product không tồn tại")
                );

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());

        if (image != null && !image.isEmpty()) {
            String oldImage = product.getImageUrl();
            if (oldImage != null && oldImage.contains("|")) {
                cloudinaryService.delete(
                        oldImage.substring(oldImage.indexOf('|') + 1)
                );
            }

            CloudinaryUploadResult result = cloudinaryService.upload(image);
            product.setImageUrl(result.url() + "|" + result.publicId());
        }

        return mapper.toDTO(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Product không tồn tại")
                );

        String image = product.getImageUrl();
        if (image != null && image.contains("|")) {
            cloudinaryService.delete(image.substring(image.indexOf('|') + 1));
        }

        productRepository.delete(product);
    }

    @Override
    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUser(Long userId) {
        return productRepository.countByUserId(userId);
    }
}
