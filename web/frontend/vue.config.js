module.exports = {

  outputDir: "dist",
  assetsDir: "static",
  publicPath: "/",

  devServer: {
    proxy: {
      "/api": {
        target: {
          host: "localhost",
          protocol: 'http',
          port:8090,
        },
        changeOrigin: true,
      },
      "/jobs": {
        target: {
          host: "localhost",
          protocol: 'ws',
          port:8090
        },
        ws: true,
        changeOrigin: true
      }
    },
  },
}
