import React from 'react';
import { ChordExplorer } from './components/ChordExplorer';
import { createGlobalStyle } from 'styled-components';

const GlobalStyle = createGlobalStyle`
  * {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
  }

  body {
    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen',
      'Ubuntu', 'Cantarell', 'Fira Sans', 'Droid Sans', 'Helvetica Neue',
      sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    background-color: #ecf0f1;
    color: #2c3e50;
    line-height: 1.6;
  }

  code {
    font-family: source-code-pro, Menlo, Monaco, Consolas, 'Courier New',
      monospace;
  }

  select, input, button {
    font-family: inherit;
  }

  a {
    color: #3498db;
    text-decoration: none;
  }

  a:hover {
    text-decoration: underline;
  }
`;

function App() {
  return (
    <>
      <GlobalStyle />
      <ChordExplorer />
    </>
  );
}

export default App;
