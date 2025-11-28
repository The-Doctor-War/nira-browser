# Gesture Fixes - Swipe Up Visibility & Drag Out Issues

## Issues Fixed

### 1. Swipe-Up Delete Not Visible
**Problem**: Tab disappeared below web content when dragged up outside container bounds.

**Solution**:
- Added `tabView.bringToFront()` when drag starts
- Set elevation to 16-20dp to ensure tab stays above web content
- Applied elevation in both drag motion and animation phases

**Changes**:
```kotlin
// In setupTabGestures() - ACTION_MOVE
tabView.bringToFront()
tabView.elevation = 16f * itemView.resources.displayMetrics.density

// In animateTabDelete()
tabView.bringToFront()
tabView.elevation = 20f * itemView.resources.displayMetrics.density
```

### 2. Drag Out of Groups Not Working
**Problem**: Gesture detection conflicted - horizontal drag triggered without long-press.

**Solution**:
- Fixed gesture priority: Long-press MUST complete before horizontal drag
- Changed condition: `if (isLongPressTriggered && Math.abs(deltaX) > 20)`
- Removed premature horizontal drag detection
- Improved visual feedback with scale animation during long-press
- Added elevation management for drag-out animation

**Changes**:
```kotlin
// Gesture detection now requires long-press first
if (isLongPressTriggered && Math.abs(deltaX) > 20) {
    isHorizontalDrag = true  // Only after long-press
}

// Immediate swipe-up works WITHOUT long-press
else if (deltaY > 20 && Math.abs(deltaX) < 50 && !isLongPressTriggered) {
    isDragging = true  // Quick swipe up
}
```

**Gesture Flow**:
1. **Quick swipe up** → Delete (no long-press needed)
2. **Long-press (500ms) → Drag horizontally** → Ungroup
3. **Long-press → Release** → Spring back

### 3. Enhanced Visual Feedback

**Swipe-Up Delete**:
- Elevation: 16-20dp during drag and animation
- Always visible above all content
- Rotation, scale, alpha effects maintained

**Drag-Out Ungroup**:
- Long-press feedback: Scale to 1.15x
- During drag: Scale decreases smoothly to 1.0x
- Elevation: 12-16dp
- Flying out animation with rotation

**Spring Back**:
- OvershootInterpolator for satisfying bounce
- Resets elevation to 4dp default
- Smooth 200ms transition

## Testing Instructions

### Test Swipe-Up Delete Visibility
1. Create an island with multiple tabs
2. Swipe a tab upward quickly
3. ✅ Tab should remain visible as it goes up
4. ✅ Should see rotation and breaking apart animation
5. ✅ Tab should not disappear behind web content

### Test Drag-Out of Groups
1. Create an island with 3+ tabs
2. Long-press a tab and hold for 500ms
3. ✅ Tab should scale up to 1.15x (visual feedback)
4. While still holding, drag left or right
5. ✅ Tab should move horizontally and fade
6. Release when moved 100px+
7. ✅ Tab should fly out with rotation animation
8. ✅ Tab should be removed from island

### Test Gesture Conflicts Don't Occur
1. Quick swipe up (no hold) → Should delete
2. Long-press (500ms) → Quick release → Should spring back
3. Long-press → Drag horizontal → Should ungroup
4. Regular tap → Should switch to tab

## Key Improvements

✅ **Z-Index Management**: `bringToFront()` + elevation ensures visibility
✅ **Gesture Priority**: Long-press required before horizontal drag
✅ **Visual Clarity**: Distinct feedback for each gesture mode
✅ **No Conflicts**: Swipe-up and drag-out are clearly separated
✅ **Smooth Animations**: Interpolators make movements feel natural

## Technical Details

**Elevation Hierarchy**:
- Default tabs: 4dp
- Drag-out ungroup: 12-16dp
- Swipe-up delete: 16-20dp
- Web content: < 16dp (below dragged tabs)

**Timing**:
- Long-press detection: 500ms
- Delete animation: 250ms + 100ms poof
- Ungroup animation: 250ms
- Spring back: 200ms

**Thresholds**:
- Vertical delete: 100px upward + <50px horizontal deviation
- Horizontal ungroup: 100px sideways (after long-press)
- Long-press trigger: 20px minimum movement

All gestures include haptic feedback for tactile confirmation.
