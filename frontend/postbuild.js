const fs = require('fs');
const path = require('path');

const browserDir = path.join(__dirname, 'dist', 'frontend', 'browser');

if (fs.existsSync(browserDir)) {
  const indexHtml = path.join(browserDir, 'index.html');

  // 1. Create 404.html for static site fallback (Render, Vercel, Netlify)
  fs.copyFileSync(indexHtml, path.join(browserDir, '404.html'));

  // 2. Create physical directory index.html for known client routes
  const routes = ['student', 'college', 'aicte'];
  routes.forEach(route => {
    const routeDir = path.join(browserDir, route);
    fs.mkdirSync(routeDir, { recursive: true });
    fs.copyFileSync(indexHtml, path.join(routeDir, 'index.html'));
  });

  console.log('✓ SPA static routes and 404.html generated successfully.');
}
