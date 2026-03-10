/*-
 * #%L
 * Chat Assistant Add-on
 * %%
 * Copyright (C) 2023 - 2026 Flowing Code
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

// Combined resize functionality for all directions
window.fcChatAssistantResize = (item, container, popoverTag, sizeRaw, maxSizeRaw, direction) => {
    // Prevent duplicate initialization
    const guard = `__fcChatAssistantResize_${direction}`;
    if (item[guard]) {
        return;
    }
    item[guard] = true;

    const size = parseFloat(sizeRaw);
    const maxSize = parseFloat(maxSizeRaw);
    const overlayTag = "vaadin-popover-overlay".toUpperCase();

    let minWidth = 0;
    let minHeight = 0;
    let maxWidth = Infinity;
    let maxHeight = Infinity;
    let overlay;
    let isDragging = false;

    const directionConfig = {
        'top': {
            shouldDrag: () => overlay?.style?.bottom && !overlay?.style?.top,
            handleResize: (e) => {
                const offsetY = container.getBoundingClientRect().top - e.clientY;
                const newHeight = offsetY + container.clientHeight;
                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    container.style.height = newHeight + 'px';
                }
            },
            setupDrag: () => {
                item.style.height = maxSize + 'px';
                item.style.marginTop = -(maxSize / 2) + 'px';
            },
            cleanupDrag: () => {
                item.style.height = size + 'px';
                item.style.marginTop = '';
            }
        },
        'top-right': {
            shouldDrag: () => {
                const topRule = overlay?.style?.bottom && !overlay?.style?.top;
                const rightRule = overlay?.style?.left && !overlay?.style?.right;
                return topRule && rightRule;
            },
            handleResize: (e) => {
                const offsetY = container.getBoundingClientRect().top - e.clientY;
                const newHeight = offsetY + container.clientHeight;
                if(newHeight >= minHeight && newHeight <= maxHeight) {
                    container.style.height = newHeight + 'px';
                }
                const offsetX = e.clientX - container.getBoundingClientRect().right;
                const newWidth = offsetX + container.clientWidth;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    container.style.width = newWidth + 'px';
                }
            },
            setupDrag: () => {
                item.style.height = maxSize + 'px';
                item.style.width = maxSize + 'px';
                item.style.marginTop = -(maxSize / 2) + 'px';
                item.style.marginRight = -(maxSize / 2) + 'px';
            },
            cleanupDrag: () => {
                item.style.height = size + 'px';
                item.style.width = size + 'px';
                item.style.marginTop = '';
                item.style.marginRight = '';
            }
        },
        'right': {
            shouldDrag: () => overlay?.style?.left && !overlay?.style?.right,
            handleResize: (e) => {
                const offsetX = e.clientX - container.getBoundingClientRect().right;
                const newWidth = offsetX + container.clientWidth;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    container.style.width = newWidth + 'px';
                }
            },
            setupDrag: () => {
                item.style.width = maxSize + 'px';
                item.style.marginRight = -(maxSize / 2) + 'px';
            },
            cleanupDrag: () => {
                item.style.width = size + 'px';
                item.style.marginRight = '';
            }
        },
        'bottom-right': {
            shouldDrag: () => {
                const bottomRule = !overlay?.style?.bottom && overlay?.style?.top;
                const rightRule = overlay?.style?.left && !overlay?.style?.right;
                return bottomRule && rightRule;
            },
            handleResize: (e) => {
                const offsetY = e.clientY - container.getBoundingClientRect().bottom;
                const newHeight = offsetY + container.clientHeight;
                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    container.style.height = newHeight + 'px';
                }
                const offsetX = e.clientX - container.getBoundingClientRect().right;
                const newWidth = offsetX + container.clientWidth;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    container.style.width = newWidth + 'px';
                }
            },
            setupDrag: () => {
                item.style.height = maxSize + 'px';
                item.style.width = maxSize + 'px';
                item.style.marginBottom = -(maxSize / 2) + 'px';
                item.style.marginRight = -(maxSize / 2) + 'px';
            },
            cleanupDrag: () => {
                item.style.height = size + 'px';
                item.style.width = size + 'px';
                item.style.marginBottom = '';
                item.style.marginRight = '';
            }
        },
        'bottom': {
            shouldDrag: () => !overlay?.style?.bottom && overlay?.style?.top,
            handleResize: (e) => {
                const offsetY = e.clientY - container.getBoundingClientRect().bottom;
                const newHeight = offsetY + container.clientHeight;
                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    container.style.height = newHeight + 'px';
                }
            },
            setupDrag: () => {
                item.style.height = maxSize + 'px';
                item.style.marginBottom = -(maxSize / 2) + 'px';
            },
            cleanupDrag: () => {
                item.style.height = size + 'px';
                item.style.marginBottom = '';
            }
        },
        'bottom-left': {
            shouldDrag: () => {
                const bottomRule = !overlay?.style?.bottom && overlay?.style?.top;
                const leftRule = overlay?.style?.right && !overlay?.style?.left;
                return bottomRule && leftRule;
            },
            handleResize: (e) => {
                const offsetY = e.clientY - container.getBoundingClientRect().bottom;
                const newHeight = offsetY + container.clientHeight;
                if(newHeight >= minHeight && newHeight <= maxHeight) {
                    container.style.height = newHeight + 'px';
                }
                const offsetX = container.getBoundingClientRect().left - e.clientX;
                const newWidth = offsetX + container.clientWidth;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    container.style.width = newWidth + 'px';
                }
            },
            setupDrag: () => {
                item.style.height = maxSize + 'px';
                item.style.width = maxSize + 'px';
                item.style.marginBottom = -(maxSize / 2) + 'px';
                item.style.marginLeft = -(maxSize / 2) + 'px';
            },
            cleanupDrag: () => {
                item.style.height = size + 'px';
                item.style.width = size + 'px';
                item.style.marginBottom = '';
                item.style.marginLeft = '';
            }
        },
        'left': {
            shouldDrag: () => overlay?.style?.right && !overlay?.style?.left,
            handleResize: (e) => {
                const offsetX = container.getBoundingClientRect().left - e.clientX;
                const newWidth = offsetX + container.clientWidth;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    container.style.width = newWidth + 'px';
                }
            },
            setupDrag: () => {
                item.style.width = maxSize + 'px';
                item.style.marginLeft = -(maxSize / 2) + 'px';
            },
            cleanupDrag: () => {
                item.style.width = size + 'px';
                item.style.marginLeft = '';
            }
        },
        'top-left': {
            shouldDrag: () => {
                const topRule = overlay?.style?.bottom && !overlay?.style?.top;
                const leftRule = overlay?.style?.right && !overlay?.style?.left;
                return topRule && leftRule;
            },
            handleResize: (e) => {
                const offsetY = container.getBoundingClientRect().top - e.clientY;
                const newHeight = offsetY + container.clientHeight;
                if(newHeight >= minHeight && newHeight <= maxHeight) {
                    container.style.height = newHeight + 'px';
                }
                const offsetX = container.getBoundingClientRect().left - e.clientX;
                const newWidth = offsetX + container.clientWidth;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    container.style.width = newWidth + 'px';
                }
            },
            setupDrag: () => {
                item.style.height = maxSize + 'px';
                item.style.width = maxSize + 'px';
                item.style.marginTop = -(maxSize / 2) + 'px';
                item.style.marginLeft = -(maxSize / 2) + 'px';
            },
            cleanupDrag: () => {
                item.style.height = size + 'px';
                item.style.width = size + 'px';
                item.style.marginTop = '';
                item.style.marginLeft = '';
            }
        }
    };

    const config = directionConfig[direction];
    if (!config) {
        console.error(`Invalid direction: ${direction}. Valid directions: ${Object.keys(directionConfig).join(', ')}`);
        return;
    }

    window.requestAnimationFrame(fetchOverlay);
    setTimeout(fetchOverlay, 2000); // in case the overlay is not available immediately, check again after 2 seconds

    // Fetch the root overlay component
    function fetchOverlay() {
        if (!overlay) {
            overlay = document.querySelector(`.${popoverTag}`)?.shadowRoot?.querySelector(overlayTag);
            if(!overlay) {
                overlay = [...document.getElementsByClassName(popoverTag)].find(p => p.tagName == overlayTag);
            }
        }
    }

    item.addEventListener('pointerenter', (e) => {
        if (config.shouldDrag()) {
            item.classList.add('active');
            const computedStyle = window.getComputedStyle(container);
            minHeight = computedStyle.minHeight ? parseFloat(computedStyle.minHeight) || 0 : 0;
            minWidth = computedStyle.minWidth ? parseFloat(computedStyle.minWidth) || 0 : 0;
            maxWidth = computedStyle.maxWidth ? parseFloat(computedStyle.maxWidth) || Infinity : Infinity;
            maxHeight = computedStyle.maxHeight ? parseFloat(computedStyle.maxHeight) || Infinity : Infinity;
        }
        else {
            item.classList.remove('active');
        }
    });

    item.addEventListener('pointerdown', (e) => {
        isDragging = config.shouldDrag();
        if (isDragging) {
            item.setPointerCapture(e.pointerId);
            config.setupDrag();
        }
    });

    item.addEventListener('pointermove', (e) => {
        if (!isDragging) return;
        config.handleResize(e);
    });

    item.addEventListener('pointerup', (e) => stopDragging(e));
    item.addEventListener('pointerleave', (e) => stopDragging(e));
    item.addEventListener('pointercancel', (e) => stopDragging(e));

    function stopDragging(e) {
        const wasDragging = isDragging;
        isDragging = false;
        item.classList.remove('active');
        if (wasDragging) {
            config.cleanupDrag();
            if (item.hasPointerCapture(e.pointerId)) {
                item.releasePointerCapture(e.pointerId);
            }
        }
    }
};
