import type { ReactElement } from 'react';
import React from 'react';
import {useState} from 'react';
import Draggable from 'react-draggable';
import Fab from '@mui/material/Fab';
import { createTheme, ThemeProvider } from '@mui/material/styles';
import { ReactAdapterElement, type RenderHooks } from 'Frontend/generated/flow/ReactAdapter';

const lumoTheme = createTheme({
  palette: {
    primary: {
      main: 'var(--lumo-primary-color)',
      light: 'var(--lumo-primary-color-50pct)',
      dark: 'var(--lumo-primary-color-20pct)',
      contrastText: 'rgb(var(--lumo-primary-contrast-color))',
    },
    },
    components: {
        MuiFab: {
          styleOverrides: {
            root: ({ theme }) => ({
              backgroundColor: theme.palette.primary.main,
              color: theme.palette.primary.contrastText,
              '&:hover': {
                backgroundColor: 'var(--lumo-primary-color-50pct)', 
              },
            }),
          },
        },
     }
     });

  
class AnimatedFABElement extends ReactAdapterElement {
  private draggableNodeRef = React.createRef<HTMLDivElement>();
  
  protected override render(hooks: RenderHooks): ReactElement | null {
    const [isDragging, setIsDragging] = useState<boolean>(false);
    const eventControl = (event: { type: any; }) => {
        if (event.type === 'mousemove' || event.type === 'touchmove') {
          setIsDragging(true)
        }
        if (event.type === 'mouseup' || event.type === 'touchend') {
          setTimeout(() => {
            setIsDragging(false);
          }, 100);
        }
      }
    return (
      <ThemeProvider theme={lumoTheme}>
        <Draggable 
          nodeRef={this.draggableNodeRef}
          onDrag={eventControl}
          onStop={eventControl}
        >
          <div
              onClick={(event) => {if (!isDragging) {this.dispatchEvent(new CustomEvent('avatar-clicked'));}}}
              onTouchEndCapture={(event) => {if (!isDragging) {this.dispatchEvent(new CustomEvent('avatar-clicked'));}}}
            ref={this.draggableNodeRef}
            style={{
              position: 'fixed',
              bottom: 16,
              right: 16
            }}
          >
            <Fab
              color="primary"
              aria-label="open chat assistant"
            >
            </Fab>
          </div>
        </Draggable>
      </ThemeProvider>
    );
  }
}

customElements.define('animated-fab', AnimatedFABElement); 
