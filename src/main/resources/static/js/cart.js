// script.js (업데이트된 최종 코드)

document.addEventListener('DOMContentLoaded', function() {
    
    let fixedDeliveryFee = 2000;
    
    // ------------------------------------------
    // A. 품목별 합산 가격 업데이트 함수 (새로 추가/수정)
    // ------------------------------------------

    // 단가와 수량을 바탕으로 각 품목의 최종 합산 가격을 업데이트합니다.
    function updateItemPriceDisplay(itemElement) {
        let itemPricePerUnit = parseInt(itemElement.dataset.price);
        let itemQuantity = parseInt(itemElement.querySelector('.item-quantity').dataset.quantity);
        let itemTotalPrice = itemPricePerUnit * itemQuantity;
        
        let priceDisplayElement = itemElement.querySelector('.item-price-display');
        
        if (priceDisplayElement) {
            priceDisplayElement.textContent = `${itemTotalPrice.toLocaleString('ko-KR')}원`;
        }
    }
    
    // ------------------------------------------
    // 1. 가격 업데이트 및 배달비 계산 함수 (유지)
    // ------------------------------------------

    function updateOrderPrice(productTotal) {
        // ... (기존 코드 유지) ...
        let orderDetails = document.querySelector('.order-details');
        if (!orderDetails) return; 

        // 상품 금액 업데이트
        let productPriceElement = document.getElementById('productPrice');
        if (productPriceElement) {
            productPriceElement.textContent = `${productTotal.toLocaleString('ko-KR')}원`;
        }

        let summaryTotalElement = document.getElementById('summaryTotalPrice');
        if (summaryTotalElement) {
            summaryTotalElement.textContent = `${productTotal.toLocaleString('ko-KR')}원`;
        }
        
        let currentDeliveryFee = 0;
        let deliveryFeeElement = document.getElementById('deliveryFee');
        
        let deliveryButton = document.querySelector('.delivery-btn[data-type="delivery"]');
        
        if (deliveryButton && deliveryButton.classList.contains('active-delivery')) {
            currentDeliveryFee = fixedDeliveryFee;
        }

        if (deliveryFeeElement) {
            deliveryFeeElement.textContent = currentDeliveryFee > 0 ? `${currentDeliveryFee.toLocaleString('ko-KR')}원` : '0원';
        }
        
        let finalTotal = productTotal + currentDeliveryFee;
        let finalTotalElement = document.getElementById('finalTotalPrice');
        
        if (finalTotalElement) {
            finalTotalElement.textContent = `${finalTotal.toLocaleString('ko-KR')}원`;
        }
    }

    // 장바구니 총 가격을 계산하고 UI를 업데이트하는 함수
    function updateCartTotal() {
        let total = 0;
        let items = document.querySelectorAll('.cart-item');
        
        items.forEach(function(item) {
            let isChecked = item.querySelector('.item-checkbox-input').checked;
            
            // 🌟 각 품목의 합산 가격을 먼저 업데이트 🌟
            updateItemPriceDisplay(item);
            
            if (isChecked) {
                let itemPricePerUnit = parseInt(item.dataset.price);
                let itemQuantity = parseInt(item.querySelector('.item-quantity').dataset.quantity);
                total += itemPricePerUnit * itemQuantity;
            }
        });

        let formattedTotal = total.toLocaleString('ko-KR');
        let cartTotalElement = document.getElementById('totalCartPrice');
        
        if (cartTotalElement) {
            cartTotalElement.textContent = `${formattedTotal}원`;
        }
        
        updateOrderPrice(total);
        
        let selectAllCheckbox = document.getElementById('selectAll');
        let remainingItems = document.querySelectorAll('.cart-item').length;
        if (selectAllCheckbox && remainingItems === 0) {
             selectAllCheckbox.checked = false;
        }
    }
    
    // ------------------------------------------
    // 2. 장바구니 항목 기능 (수량/삭제/체크박스)
    // ------------------------------------------
    let cartContainer = document.querySelector('.item-list');

    if (cartContainer) {
        cartContainer.addEventListener('click', function(e) {
            let btn = e.target;
            let item = btn.closest('.cart-item');
            if (!item) return;

            if (btn.classList.contains('plus-btn') || btn.classList.contains('minus-btn')) {
                let quantitySpan = item.querySelector('.item-quantity');
                let currentQuantity = parseInt(quantitySpan.dataset.quantity);
                let newQuantity = currentQuantity;

                if (btn.classList.contains('plus-btn')) {
                    newQuantity += 1;
                } else if (btn.classList.contains('minus-btn')) {
                    if (currentQuantity > 1) {
                        newQuantity -= 1;
                    }
                }
                
                if (newQuantity !== currentQuantity) {
                    quantitySpan.dataset.quantity = newQuantity;
                    quantitySpan.textContent = newQuantity;
                    
                    // 🌟 수량 변경 시 해당 품목 가격 및 총 가격 업데이트 🌟
                    updateItemPriceDisplay(item); 
                    updateCartTotal();
                }

            } else if (btn.classList.contains('item-remove')) {
                item.remove();
                updateCartTotal();
            } else if (btn.classList.contains('item-checkbox-input')) {
                let selectAllCheckbox = document.getElementById('selectAll');
                let allChecked = Array.from(document.querySelectorAll('.item-checkbox-input')).every(cb => cb.checked);
                if (selectAllCheckbox) {
                    selectAllCheckbox.checked = allChecked;
                }
                updateCartTotal();
            }
        });

        // 전체 선택/해제 기능
        let selectAllCheckbox = document.getElementById('selectAll');
        if(selectAllCheckbox) {
             selectAllCheckbox.addEventListener('change', function() {
                let itemCheckboxes = document.querySelectorAll('.item-checkbox-input');
                itemCheckboxes.forEach(function(checkbox) {
                    checkbox.checked = selectAllCheckbox.checked;
                });
                updateCartTotal();
            });
        }
        
        // 초기 로드 시 총 가격 계산 (품목별 가격도 이때 업데이트됨)
        updateCartTotal();
    }
    
    // ------------------------------------------
    // 3. 주문 상세 기능 (배달/포장 토글) (유지)
    // ------------------------------------------
    let deliveryToggle = document.querySelector('.delivery-toggle');

    if (deliveryToggle) {
        deliveryToggle.addEventListener('click', function(e) {
            if (e.target.classList.contains('delivery-btn')) {
                deliveryToggle.querySelectorAll('.delivery-btn').forEach(function(btn) {
                    btn.classList.remove('active-delivery');
                });
                
                e.target.classList.add('active-delivery');
                updateCartTotal(); 
            }
        });
    }

    // ------------------------------------------
    // 4. 요청사항 직접입력 활성화/비활성화 기능 (유지)
    // ------------------------------------------
    let directInputCheckbox = document.getElementById('directInputCheck');
    let requestInputTextarea = document.getElementById('requestInput');

    if (directInputCheckbox && requestInputTextarea) {
        directInputCheckbox.addEventListener('change', function() {
            let isChecked = directInputCheckbox.checked;
            requestInputTextarea.disabled = !isChecked;
            
            if (!isChecked) {
                requestInputTextarea.value = '';
            }
        });
        
        requestInputTextarea.disabled = !directInputCheckbox.checked;
    }

    // ------------------------------------------
    // 5. 선택 삭제 기능 (유지)
    // ------------------------------------------
    let deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    
    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', function() {
            let checkedItems = document.querySelectorAll('.cart-item .item-checkbox-input:checked');
            
            if (checkedItems.length === 0) {
                alert('삭제할 항목을 선택해주세요.');
                return;
            }

            if (!confirm(`선택된 ${checkedItems.length}개의 항목을 장바구니에서 삭제하시겠습니까?`)) {
                return;
            }
            
            checkedItems.forEach(function(checkbox) {
                let cartItem = checkbox.closest('.cart-item');
                if (cartItem) {
                    cartItem.remove();
                }
            });
            
            updateCartTotal();
            
            let selectAllCheckbox = document.getElementById('selectAll');
            if (selectAllCheckbox) {
                selectAllCheckbox.checked = false;
            }
        });
    }
});