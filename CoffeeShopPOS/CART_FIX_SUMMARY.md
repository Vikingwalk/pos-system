# Cart Remove Functionality Fix

## Date: December 3, 2024

## Problem Identified
The `removeItemFromCart()` method in `CashierController.java` was removing the entire product line from the cart regardless of quantity. 

For example:
- If a user added 99 pizzas to the cart
- Clicking "Remove" would delete all 99 pizzas
- No option to remove only part of the quantity

## Solution Implemented

### Enhanced Remove Functionality
Modified `removeItemFromCart()` to include a dialog that asks the user how many items to remove:

**Key Features:**
1. **Dialog Prompt**: When clicking "Remove", a dialog appears asking how many items to remove
2. **Current Quantity Display**: Shows the current quantity in the cart
3. **Partial or Full Removal**: 
   - If user removes quantity equal to current quantity → removes entire line
   - If user removes less than current quantity → reduces quantity and keeps line in cart
4. **Input Validation**:
   - Must be greater than 0
   - Cannot exceed current quantity in cart
   - Proper error messages for invalid input
5. **Styled Dialog**: Dark theme matching the application design

### Code Changes

**File Modified**: `src/controllers/CashierController.java`

**Before** (lines 392-402):
```java
/**
 * Remove a single item from the cart
 */
private void removeItemFromCart(Product item) {
    if (item == null) return;
    
    cartList.remove(item);
    cartTable.refresh();
    updateTotals();
    
    System.out.println("Removed item: " + item.getName() + " from cart");
}
```

**After** (lines 392-465):
```java
/**
 * Remove item from cart with quantity dialog
 */
private void removeItemFromCart(Product item) {
    if (item == null) return;
    
    int currentQuantity = item.getStock(); // In cart, stock field holds quantity
    
    // Create dialog to ask how many to remove
    TextInputDialog dialog = new TextInputDialog(String.valueOf(currentQuantity));
    dialog.setTitle("Remove from Cart");
    dialog.setHeaderText("Remove: " + item.getName());
    dialog.setContentText("Current quantity: " + currentQuantity + "\nEnter quantity to remove:");
    
    // Style the dialog
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setStyle("-fx-background-color: #2c3e50;");
    dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #ecf0f1;");
    dialogPane.lookup(".header-panel").setStyle("-fx-background-color: #34495e;");
    dialogPane.lookup(".header-panel .label").setStyle("-fx-text-fill: #ecf0f1;");
    
    dialog.showAndWait().ifPresent(input -> {
        try {
            int quantityToRemove = Integer.parseInt(input.trim());
            
            if (quantityToRemove <= 0) {
                showAlert("Input Error", "Quantity must be greater than 0");
                return;
            }
            
            if (quantityToRemove > currentQuantity) {
                showAlert("Input Error", "Cannot remove more than current quantity (" + currentQuantity + ")");
                return;
            }
            
            if (quantityToRemove == currentQuantity) {
                // Remove entire item
                cartList.remove(item);
                System.out.println("Removed all " + currentQuantity + " of " + item.getName() + " from cart");
            } else {
                // Reduce quantity
                int newQuantity = currentQuantity - quantityToRemove;
                
                // Find the item in cart and update its quantity
                for (int i = 0; i < cartList.size(); i++) {
                    Product cartItem = cartList.get(i);
                    if (cartItem.getId() == item.getId()) {
                        // Create updated product with new quantity
                        Product updatedItem = new Product(
                            cartItem.getId(),
                            cartItem.getName(),
                            cartItem.getDescription(),
                            cartItem.getPrice(),
                            newQuantity, // Updated quantity
                            cartItem.getCategory(),
                            true
                        );
                        cartList.set(i, updatedItem);
                        System.out.println("Reduced " + item.getName() + " quantity from " + 
                                         currentQuantity + " to " + newQuantity);
                        break;
                    }
                }
            }
            
            cartTable.refresh();
            updateTotals();
            
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid number");
        }
    });
}
```

## Testing Checklist

- [ ] Add 99 pizzas to cart
- [ ] Click Remove button on pizza line
- [ ] Dialog appears with current quantity (99)
- [ ] Enter 50 and click OK
- [ ] Verify cart now shows 49 pizzas remaining
- [ ] Verify subtotal and total are updated correctly
- [ ] Click Remove again
- [ ] Enter 49 and click OK
- [ ] Verify pizza line is completely removed from cart
- [ ] Test with quantity = 0 (should show error)
- [ ] Test with quantity > current (should show error)
- [ ] Test with invalid input like "abc" (should show error)
- [ ] Test Cancel button (should not change cart)

## Usage Instructions

### For Users
1. Add items to cart as usual
2. To remove items, click the "Remove" button in the cart table
3. A dialog will appear asking how many items to remove
4. Enter the quantity:
   - Enter the full quantity to remove the entire line
   - Enter a smaller number to reduce the quantity
5. Click OK to confirm or Cancel to abort

### Example Scenarios

**Scenario 1: Remove All Items**
- Cart has: 99x Pizza
- Click Remove → Enter 99 → OK
- Result: Pizza removed from cart

**Scenario 2: Partial Removal**
- Cart has: 99x Pizza
- Click Remove → Enter 50 → OK
- Result: Cart now has 49x Pizza

**Scenario 3: Multiple Partial Removals**
- Cart has: 99x Pizza
- Click Remove → Enter 30 → OK
- Cart has: 69x Pizza
- Click Remove → Enter 20 → OK
- Cart has: 49x Pizza

## Benefits
1. **Flexibility**: Users can remove exact quantities they need
2. **Efficiency**: No need to completely remove and re-add items
3. **User-Friendly**: Clear dialog with current quantity information
4. **Error Prevention**: Input validation prevents mistakes
5. **Consistent UX**: Styled to match application theme

## Technical Notes
- Uses `TextInputDialog` from JavaFX (already imported via wildcard)
- Maintains existing cart structure (uses Product.stock field for quantity)
- Properly updates totals after any change
- Console logging for debugging
- No database changes required
- Backward compatible with existing code

## No Breaking Changes
- All existing functionality preserved
- addToCart() method unchanged
- checkout() method unchanged
- Cart table structure unchanged
- Only the remove behavior is enhanced

---

**Status**: ✅ Implemented and Ready for Testing
**Impact**: Low Risk - Isolated change to single method
**Testing Required**: Manual UI testing with various quantities
