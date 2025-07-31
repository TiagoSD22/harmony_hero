'use client';

import { useState, useEffect } from 'react';
import styled from 'styled-components';

interface ParallaxBackgroundProps {
  children: React.ReactNode;
}

const ParallaxContainer = styled.div`
  position: relative;
  min-height: 100vh;
  overflow: hidden;
`;

const GuitarNeckBackground = styled.div<{ $mouseX: number; $mouseY: number }>`
  position: fixed;
  top: 0;
  left: 0;
  width: 120%;
  height: 120%;
  background: linear-gradient(
    90deg,
    #2d1810 0%,
    #3d2415 20%,
    #4a2c18 40%,
    #3d2415 60%,
    #2d1810 80%,
    #1a0f08 100%
  );
  transform: 
    translateX(${props => -props.$mouseX * 0.02}px) 
    translateY(${props => -props.$mouseY * 0.01}px);
  transition: transform 0.1s ease-out;
  z-index: -3;
  
  &:before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: repeating-linear-gradient(
      90deg,
      transparent 0px,
      transparent 140px,
      rgba(139, 69, 19, 0.15) 140px,
      rgba(139, 69, 19, 0.15) 142px
    );
  }
  
  &:after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: repeating-linear-gradient(
      0deg,
      transparent 0px,
      transparent 48px,
      rgba(210, 180, 140, 0.1) 48px,
      rgba(210, 180, 140, 0.1) 50px
    );
  }
`;

const WoodGrain = styled.div<{ $mouseX: number; $mouseY: number }>`
  position: fixed;
  top: 0;
  left: 0;
  width: 110%;
  height: 110%;
  background-image: 
    radial-gradient(ellipse at center, rgba(139, 69, 19, 0.1) 0%, transparent 50%),
    linear-gradient(90deg, 
      rgba(101, 67, 33, 0.2) 0%,
      rgba(160, 82, 45, 0.1) 25%,
      rgba(101, 67, 33, 0.2) 50%,
      rgba(160, 82, 45, 0.1) 75%,
      rgba(101, 67, 33, 0.2) 100%
    );
  background-size: 300px 200px, 80px 100%;
  transform: 
    translateX(${props => -props.$mouseX * 0.015}px) 
    translateY(${props => -props.$mouseY * 0.008}px);
  transition: transform 0.15s ease-out;
  z-index: -2;
`;

const Frets = styled.div<{ $mouseX: number; $mouseY: number }>`
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transform: 
    translateX(${props => -props.$mouseX * 0.025}px);
  transition: transform 0.1s ease-out;
  z-index: -1;
  
  &:before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: repeating-linear-gradient(
      90deg,
      transparent 0px,
      transparent 120px,
      rgba(192, 192, 192, 0.3) 120px,
      rgba(192, 192, 192, 0.3) 124px,
      transparent 124px,
      transparent 140px,
      rgba(192, 192, 192, 0.2) 140px,
      rgba(192, 192, 192, 0.2) 142px
    );
  }
`;

const ContentOverlay = styled.div`
  position: relative;
  z-index: 1;
  background: rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(1px);
  min-height: 100vh;
`;

const Strings = styled.div<{ $mouseX: number; $mouseY: number }>`
  position: fixed;
  top: 0;
  left: 0;
  width: 105%;
  height: 100%;
  transform: 
    translateX(${props => -props.$mouseX * 0.01}px) 
    translateY(${props => -props.$mouseY * 0.005}px);
  transition: transform 0.2s ease-out;
  z-index: -1;
  pointer-events: none;
  
  &:before {
    content: '';
    position: absolute;
    top: 20%;
    left: 0;
    right: 0;
    height: 60%;
    background: repeating-linear-gradient(
      0deg,
      transparent 0%,
      transparent 9%,
      rgba(220, 220, 220, 0.1) 9.5%,
      rgba(220, 220, 220, 0.1) 10%,
      transparent 10.5%,
      transparent 15%,
      rgba(200, 200, 200, 0.08) 15.5%,
      rgba(200, 200, 200, 0.08) 16%,
      transparent 16.5%
    );
  }
`;

function ParallaxBackground({ children }: ParallaxBackgroundProps) {
  const [mousePosition, setMousePosition] = useState({ x: 0, y: 0 });

  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      const x = (e.clientX / window.innerWidth - 0.5) * 100;
      const y = (e.clientY / window.innerHeight - 0.5) * 100;
      setMousePosition({ x, y });
    };

    window.addEventListener('mousemove', handleMouseMove);
    return () => window.removeEventListener('mousemove', handleMouseMove);
  }, []);

  return (
    <ParallaxContainer>
      <GuitarNeckBackground $mouseX={mousePosition.x} $mouseY={mousePosition.y} />
      <WoodGrain $mouseX={mousePosition.x} $mouseY={mousePosition.y} />
      <Frets $mouseX={mousePosition.x} $mouseY={mousePosition.y} />
      <Strings $mouseX={mousePosition.x} $mouseY={mousePosition.y} />
      <ContentOverlay>
        {children}
      </ContentOverlay>
    </ParallaxContainer>
  );
}

export { ParallaxBackground };
