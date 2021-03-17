module.exports = {

  outputDir: "dist",
  assetsDir: "static",
  publicPath: "/nedrex/",

  devServer: {
    proxy: {
      "/backend/api": {
        target: {
          host: "localhost",
          protocol: 'http',
          port:8090,
        },
        changeOrigin: true,
      },
      // "/backend/jobs": {
      //   target: {
      //     host: "localhost",
      //     protocol: 'ws',
      //     port:8090
      //   },
      //   ws: true,
      //   changeOrigin: true
      // }
    },
  },
}
