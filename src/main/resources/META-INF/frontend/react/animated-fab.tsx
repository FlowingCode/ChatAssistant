import type { ReactElement } from 'react';
import React from 'react';
import {useState} from 'react';
import Draggable from 'react-draggable';
import Fab from '@mui/material/Fab';
import Badge from '@mui/material/Badge';
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
    warning: {
        main: 'var(--lumo-warning-color)',
        light: 'var(--lumo-warning-color-50pct)',
        dark: 'var(--lumo-warning-color-20pct)',
        contrastText: 'rgb(var(--lumo-warning-contrast-color))',
    }
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
    const [unreadMessages] = hooks.useState<integer>('unreadMessages');
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
          <Badge badgeContent={unreadMessages} color="warning" overlap="circular">
            <Fab
              color="primary"
              aria-label="open chat assistant"
            >
            </Fab>
          </Badge>
          </div>
        </Draggable>
      </ThemeProvider>
    );
  }
}

customElements.define('animated-fab', AnimatedFABElement); 
