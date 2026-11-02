# Screenshot capture

PNG capture could not run in the packaging environment: a browser binary was obtained, but startup failed because the environment disallows the socket needed by Chrome's process singleton. No screenshots are fabricated or claimed as rendered.

The approved visual reference remains `../prototype/index.html`. Use it to inspect the design interactively. Capture native Android comparisons during each implementation PR.

To generate browser reference PNGs on your local development computer:

```powershell
cd docs/ui/prototype
npm install
npx playwright install chromium
npm run capture
```

This writes the 20 main views and extra correction/notes states to this directory and writes `../verification-browser.json`. Review images before committing; compare against the approved prototype rather than treating successful rendering alone as visual QA. Playwright/browser installation needs internet; the prototype itself has its fonts and icons embedded.

Capture script is provided but was not successfully executed in the packaging environment. A later runtime failure must be fixed before claiming screenshot verification.
