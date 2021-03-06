package com.cntt2.nowfood.service.impl;

import com.cntt2.nowfood.common.Constants;
import com.cntt2.nowfood.config.security.UserPrincipal;
import com.cntt2.nowfood.domain.Order;
import com.cntt2.nowfood.domain.Product;
import com.cntt2.nowfood.domain.ProductSize;
import com.cntt2.nowfood.domain.Shop;
import com.cntt2.nowfood.dto.order.CartDto;
import com.cntt2.nowfood.dto.order.FeeOrder;
import com.cntt2.nowfood.dto.order.OrderDto;
import com.cntt2.nowfood.dto.order.OrderSearchDto;
import com.cntt2.nowfood.exceptions.ValidException;
import com.cntt2.nowfood.mapper.OrderMapper;
import com.cntt2.nowfood.repository.OrderRepository;
import com.cntt2.nowfood.repository.ProductRepository;
import com.cntt2.nowfood.repository.UserRepository;
import com.cntt2.nowfood.service.OrderService;
import com.cntt2.nowfood.utils.CommonUtils;
import com.cntt2.nowfood.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Vanh
 * @version 1.0
 * @date 11/25/2021 10:32 PM
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends GenericServiceImpl<Order, Integer> implements
        OrderService {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    @Value("${order.ghn.token}")
    private String tokenGHN;

    @Override
    @Transactional
    public OrderDto checkout(CartDto form) {
        Order entity = orderMapper.toEntity(form);
        // L???y danh s??ch s???n ph???m c?? trong gi??? h??ng
        Set<Integer> ids = form.getCartItems()
                .stream()
                .map(CartDto.CartItemDto::getProductId)
                .collect(Collectors.toSet());
        Set<Product> products = productRepository.findByIds(ids);
        if(products.isEmpty())
            throw new ValidException("S???n ph???m trong gi??? h??ng kh??ng h???p l???!");
        setOrderDetails(form, entity, products);
        // set payment status
        entity.setPaymentStatus(Constants.PaymentStatus.UNPAID);
        // set order status
        entity.setOrderStatus(Constants.OrderStatus.PENDING);
        // set Customer order
        UserPrincipal user = SecurityUtils.getCurrentUser();
        if(user != null){
            entity.setCustomer(userRepository.findByUsername(user.getUsername()));
        }
        // set fee order
        Double shippingFee = getShippingFee(entity).getData().getTotal();
        entity.setShipPrice(shippingFee);
        // set total order
        Double totalPrice = entity.getTotalPrice();
        entity.setTotalPrice(totalPrice);
        Order result = orderRepository.save(entity);
        return orderMapper.toDto(result);
    }

    private void setOrderDetails(CartDto form, Order entity, Set<Product> products) {
        // L???y ra shop c???a s???n ph???m ?????u ti??n, so sanh v???i c??c s???n ph???m c??n l???i trong gi??? h??ng
        Product firstProduct = products.iterator().next();
        Shop shop = firstProduct.getShop();
        entity.setShop(shop);
        // set Order details
        form.getCartItems().forEach(item -> {
            Product product = products.stream().
                    filter(x -> x.getId().equals(item.getProductId())).
                    findFirst().orElseThrow(() ->
                        new ValidException("S???n ph???m id= " + item.getProductId() + " kh??ng h???p l??? !")
            );
            Double price = product.getPrice();
            String productName = product.getName();
            // N???u c?? size th?? set l???i gi?? tr??? price v?? productName theo size
            if(item.getSizeId() != null){
                // L???y product size
                ProductSize productSize = findProductSize(product,item.getSizeId());
                if(productSize != null){
                    price = productSize.getPrice();
                    productName = productName + " - " + productSize.getSize().getName();
                }
            }
            // H???t h??ng
            if(product.getIsSoldOut())
                throw new ValidException("S???n ph???m " + product.getName() + " ???? h???t h??ng !");
            // Ki???m tra xem c??c s???n ph???m c?? c??ng c???a h??ng kh??ng
            Boolean isSameShop = shop.getId().equals(product.getShop().getId());
            if (!isSameShop)
                throw new ValidException("C??c s???n ph???m trong gi??? h??ng kh??ng c??ng 1 shop!");
            entity.addOrderDetail(product,productName,price, item.getQuantity());
            // set totalOrder cho product
            Integer totalOrder = product.getTotalOrder()+item.getQuantity();
            product.setTotalOrder(totalOrder);
        });
    }

    @Override
    public FeeOrder getShippingFee(Order order) {
        RestTemplate restTemplate = new RestTemplate();
        Map<String, Object> map = new HashMap<>();
        map.put("service_id", 53321);
        map.put("insurance_value", order.getTotalPrice().intValue());
        map.put("coupon", null);
        map.put("from_district_id", 1542);
        map.put("to_district_id", 1444);
        map.put("to_ward_code", 20314);
        map.put("height", 15);
        map.put("length", 15);
        map.put("weight", 1000);
        map.put("width", 15);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("token",tokenGHN);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map,httpHeaders);
        String uri = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
        ResponseEntity<FeeOrder> response =  restTemplate.postForEntity(uri,request, FeeOrder.class);
        return response.getBody();
    }

    @Override
    public Page<OrderDto> findByAdvSearch(OrderSearchDto form) {
        Pageable pageable = CommonUtils.getPageRequest(form);
        Page<Order> ordes = orderRepository.findByAdvSearch(form,pageable);
        return ordes.map(orderMapper::toDto);
    }

    @Override
    public OrderDto findById(Integer id) {
        Order order = getOrder(id);
        return orderMapper.toDto(order);
    }

    private Order getOrder(Integer id) {
        return orderRepository.findById(id).orElseThrow(() -> new ValidException("Kh??ng t??m th???y ????n h??ng!"));
    }

    @Override
    public OrderDto approve(Integer id) {
        Order order = getOrder(id);
        if(order.getOrderStatus() != Constants.OrderStatus.PENDING){
            throw new ValidException("????n h??ng kh??ng ??? tr???ng th??i ch??? x??c nh???n !");
        }
        order.setOrderStatus(Constants.OrderStatus.CONFIRMED);
        order = orderRepository.save(order);
        return orderMapper.toDto(order);
    }
    @Override
    public OrderDto reject(Integer id) {
        Order order = getOrder(id);
        if(order.getOrderStatus() != Constants.OrderStatus.PENDING){
            throw new ValidException("????n h??ng kh??ng ??? tr???ng th??i ch??? x??c nh???n !");
        }
        order.setOrderStatus(Constants.OrderStatus.CANCELED);
        order = orderRepository.save(order);
        return orderMapper.toDto(order);
    }
    private ProductSize findProductSize(Product product,Integer sizeId){
        return product.getProductSizes().
                stream().filter(x -> x.getSize().getId().equals(sizeId)).
                findFirst().orElse(null);
    }
}
