/** @type {import('next').NextConfig} */
const nextConfig = {
  compiler: {
    styledComponents: true,
  },
  eslint: {
    // Disable ESLint during builds to avoid blocking on warnings
    ignoreDuringBuilds: true,
  },
  typescript: {
    // Skip type checking during builds (useful for development)
    ignoreBuildErrors: false,
  },
  output: 'standalone',
};

module.exports = nextConfig;
