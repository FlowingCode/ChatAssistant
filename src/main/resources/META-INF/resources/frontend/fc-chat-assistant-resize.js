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

// Combined resize functionality for all directions.
// `container` is the chat overlay Div (it fills the popover content part, so its rendered size is the
// current content size). Resizing writes the desired size onto the popover's public content-height/
// content-width, which Vaadin clamps to the viewport, so the content can never overflow the popover.
window.fcChatAssistantResize = (root, item, container, popoverTag, sizeRaw, maxSizeRaw, direction) => {
    // Prevent duplicate initialization. The handlers always attach once; whether a drag is allowed is
    // decided live (see isResizable) so toggling resizable after init takes effect immediately.
    const guard = `__fcChatAssistantResize_${direction}`;
    if (item[guard]) {
        return;
    }
    item[guard] = true;

    // The resizable state is read live at event time, not captured at init, so setWindowResizable()
    // enables/disables resizing on an already-initialized handle.
    const isResizable = () => container.hasAttribute('resizable');

    const size = parseFloat(sizeRaw);
    const maxSize = parseFloat(maxSizeRaw);

    let minWidth = 0;
    let minHeight = 0;
    let maxWidth = Infinity;
    let maxHeight = Infinity;
    let overlay;       // the vaadin-popover-overlay element (used for shouldDrag positioning rules)
    let contentPart;   // its [part='content'], which our overlay Div fills
    let isDragging = false;

    // Write the desired size directly onto the overlay's content part. This part keeps
    // overflow:auto + max-height/width:100% in both Vaadin 24 and 25, so the size is clamped to the
    // viewport and the content never overflows. (Vaadin 25 removed the content-height/width API, so
    // sizing the part directly is the version-agnostic approach.)
    // The desired size is also stored on the (durable) overlay Div so it can be restored when the
    // popover is closed and reopened, since Vaadin rebuilds the overlay's shadow DOM each time.
    const sizeTarget = () => contentPart || overlay;
    const setContentHeight = (px) => {
        container.style.setProperty('--fc-height', px + 'px');
        sizeTarget().style.height = px + 'px';
    };
    const setContentWidth = (px) => {
        container.style.setProperty('--fc-width', px + 'px');
        sizeTarget().style.width = px + 'px';
    };

    const directionConfig = {
        'top': {
            shouldDrag: () => overlay?.style?.bottom && !overlay?.style?.top,
            handleResize: (e) => {
                const offsetY = container.getBoundingClientRect().top - e.clientY;
                const newHeight = offsetY + container.clientHeight;
                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    setContentHeight(newHeight);
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
                    setContentHeight(newHeight);
                }
                const offsetX = e.clientX - container.getBoundingClientRect().right;
                const newWidth = offsetX + container.clientWidth;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    setContentWidth(newWidth);
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
                    setContentWidth(newWidth);
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
                    setContentHeight(newHeight);
                }
                const offsetX = e.clientX - container.getBoundingClientRect().right;
                const newWidth = offsetX + container.clientWidth;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    setContentWidth(newWidth);
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
                    setContentHeight(newHeight);
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
                    setContentHeight(newHeight);
                }
                const offsetX = container.getBoundingClientRect().left - e.clientX;
                const newWidth = offsetX + container.clientWidth;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    setContentWidth(newWidth);
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
                    setContentWidth(newWidth);
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
                    setContentHeight(newHeight);
                }
                const offsetX = container.getBoundingClientRect().left - e.clientX;
                const newWidth = offsetX + container.clientWidth;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    setContentWidth(newWidth);
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
        console.error(`Invalid direction: ${JSON.stringify(direction)}. Valid directions: ${Object.keys(directionConfig).join(', ')}`);
        return;
    }

    // Reflects the live "can this handle be dragged right now" state as a class, so the CSS can show the
    // direction arrowhead only on the handles that are currently draggable (see fc-chat-assistant-style.css).
    let styleObserver = null;
    function updateCanDrag() {
        item.classList.toggle('fc-chat-assistant-resize-can-drag', isResizable() && config.shouldDrag());
    }

    // shouldDrag() is a pure function of the overlay's inline top/bottom/left/right, which the popover
    // mutates on open and during cross-edge resizes. Also watch the container's `resizable` attribute so
    // toggling setWindowResizable() shows/hides the direction indicators immediately (without a hover).
    // Re-observe whenever a (new) overlay is resolved.
    function observeOverlayStyle() {
        styleObserver?.disconnect();
        styleObserver = new MutationObserver(updateCanDrag);
        styleObserver.observe(overlay, { attributes: true, attributeFilter: ['style'] });
        styleObserver.observe(container, { attributes: true, attributeFilter: ['resizable'] });
        updateCanDrag();
    }

    window.requestAnimationFrame(fetchOverlay);
    // In case the overlay is not available immediately, check again after 2 seconds. Tracked so it can
    // be cancelled on disconnect (it may otherwise fire against a torn-down component).
    const fetchOverlayTimeout = setTimeout(fetchOverlay, 2000);

    // Fetch the root overlay component and its content part. The popover rebuilds its overlay (and the
    // content part) on each open, so re-resolve whenever the cached nodes are gone or detached — keeping
    // stale (disconnected) references out of shouldDrag()/setContentWidth()/setContentHeight().
    function fetchOverlay() {
        if (!overlay || !overlay.isConnected) {
            contentPart = null; // a new overlay means the old content part is stale too
            overlay = resolveOverlay(popoverTag);
            if (overlay) {
                observeOverlayStyle();
            }
        }
        if (overlay && (!contentPart || !contentPart.isConnected)) {
            contentPart = overlay.shadowRoot?.querySelector('[part="content"]');
        }
    }

    const resizeHandler = () => updateCanDrag();
    window.addEventListener('resize', resizeHandler);

    // Teardown on detach (run by the animated-fab custom element's disconnectedCallback, defined in
    // fc-chat-assistant-movement.js): drop this direction's window resize listener, disconnect its
    // style observer, cancel the pending overlay lookup, and clear the init guards so the direction
    // re-initializes on reattach. Runs only on a genuine detach, not on popover reopen.
    (root.__fcCleanups = root.__fcCleanups || []).push(() => {
        window.removeEventListener('resize', resizeHandler);
        styleObserver?.disconnect();
        clearTimeout(fetchOverlayTimeout);
        item[guard] = false;
        root['fc-chat-assistant-resize-' + direction + '-listener'] = null;
    });

    item.addEventListener('pointerenter', (e) => {
        // Refresh the overlay/content-part references in case the popover was closed and reopened since
        // the last interaction (which rebuilds the overlay's shadow DOM).
        fetchOverlay();
        updateCanDrag();
        if (isResizable() && config.shouldDrag()) {
            item.classList.add('active');
            // Resize bounds come from custom properties on the overlay Div (set from the Java
            // setWindowMin*/Max* methods), so they don't affect the 100% Div's own layout.
            const computedStyle = window.getComputedStyle(container);
            const bound = (prop, dflt) => {
                const value = parseFloat(computedStyle.getPropertyValue(prop));
                return Number.isFinite(value) ? value : dflt;
            };
            minHeight = bound('--fc-min-height', 0);
            minWidth = bound('--fc-min-width', 0);
            maxHeight = bound('--fc-max-height', Infinity);
            maxWidth = bound('--fc-max-width', Infinity);
        }
        else {
            item.classList.remove('active');
        }
    });

    item.addEventListener('pointerdown', (e) => {
        isDragging = isResizable() && config.shouldDrag();
        if (isDragging) {
            item.setPointerCapture(e.pointerId);
            // The handle grows while dragging (setupDrag), which would push the arrowhead outside the
            // overlay; hide it for the duration of the resize.
            item.classList.add('fc-chat-assistant-resize-resizing');
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
        item.classList.remove('fc-chat-assistant-resize-resizing');
        if (wasDragging) {
            config.cleanupDrag();
            if (item.hasPointerCapture(e.pointerId)) {
                item.releasePointerCapture(e.pointerId);
            }
            // A resize can pin/unpin edges, changing which handles are draggable.
            updateCanDrag();
        }
    }
};

// Resolves the popover's overlay element across Vaadin 24 (the overlay carries the class directly) and
// Vaadin 25 (the overlay lives inside the popover's shadow root). Shared by fetchOverlay() and
// fcChatAssistantContentPart() so the selector chain is defined in one place.
function resolveOverlay(popoverTag) {
    const overlayTag = "vaadin-popover-overlay".toUpperCase();
    return document.querySelector(`.${popoverTag}`)?.shadowRoot?.querySelector(overlayTag)
        || [...document.getElementsByClassName(popoverTag)].find(p => p.tagName === overlayTag);
}

// Resolves the popover overlay's [part='content'] element, retrying briefly because the overlay is
// (re)created lazily when the popover opens.
function fcChatAssistantContentPart(popoverTag, callback, attempts = 0) {
    const overlay = resolveOverlay(popoverTag);
    const contentPart = overlay?.shadowRoot?.querySelector('[part="content"]');
    if (contentPart) {
        callback(contentPart);
    } else if (attempts < 20) {
        setTimeout(() => fcChatAssistantContentPart(popoverTag, callback, attempts + 1), 100);
    }
}

// Applies the chat window's configured size and bounds to the popover content part, sourced from the
// `--fc-*` custom properties on the durable overlay Div (which survive close/reopen; the content part is
// rebuilt each open). This is the single source of truth used on open and whenever a Java size/bound
// setter runs. The content part is sized directly (works on Vaadin 24 and 25), and the desired
// width/height are clamped into the [min, max] range so an explicit/previously-resized size that violates
// a (later-set) bound is corrected. Setting the part directly is required because a `var()` on
// ::part(content) cannot read a custom property set on the inner overlay Div (custom properties inherit
// downward, and the overlay Div is a descendant of the content part).
window.fcChatAssistantApplyConstraints = (overlayDiv, popoverTag) => {
    const raw = (prop) => overlayDiv.style.getPropertyValue(prop);
    // Numeric bound in px, or the default for non-px units (%/vw/…), which are left to the CSS
    // min/max the content part already carries rather than clamped numerically here.
    const num = (prop, dflt) => {
        const rawValue = raw(prop);
        if (!/^\s*\d*\.?\d+(px)?\s*$/.test(rawValue)) {
            return dflt;
        }
        const value = parseFloat(rawValue);
        return Number.isFinite(value) ? value : dflt;
    };
    const widthRaw = raw('--fc-width');
    const heightRaw = raw('--fc-height');
    const minWidth = raw('--fc-min-width');
    const minHeight = raw('--fc-min-height');
    const maxWidth = raw('--fc-max-width');
    const maxHeight = raw('--fc-max-height');

    // Clamp a desired size string into [min, max] (min wins if they cross, matching CSS). Only plain
    // px/number lengths are clamped numerically; other units (%/vw/vh/rem/…) pass through verbatim
    // and are left to the CSS min/max-width already applied to the content part.
    const clamp = (valueRaw, minProp, maxProp) => {
        if (!/^\s*\d*\.?\d+(px)?\s*$/.test(valueRaw)) {
            return valueRaw;
        }
        const value = parseFloat(valueRaw);
        if (!Number.isFinite(value)) {
            return valueRaw;
        }
        let result = Math.min(value, num(maxProp, Infinity));
        result = Math.max(result, num(minProp, 0));
        return result + 'px';
    };

    fcChatAssistantContentPart(popoverTag, (contentPart) => {
        if (minWidth) contentPart.style.minWidth = minWidth;
        if (minHeight) contentPart.style.minHeight = minHeight;
        if (maxWidth) contentPart.style.maxWidth = maxWidth;
        if (maxHeight) contentPart.style.maxHeight = maxHeight;
        if (widthRaw) contentPart.style.width = clamp(widthRaw, '--fc-min-width', '--fc-max-width');
        if (heightRaw) contentPart.style.height = clamp(heightRaw, '--fc-min-height', '--fc-max-height');
    });
};

// Re-applies the desired size and bounds (stored on the overlay Div) to the content part. Called whenever
// the popover opens, because Vaadin rebuilds the overlay's shadow DOM and the inline size is lost.
window.fcChatAssistantRestoreWindowSize = (overlayDiv, popoverTag) => {
    window.fcChatAssistantApplyConstraints(overlayDiv, popoverTag);
};
