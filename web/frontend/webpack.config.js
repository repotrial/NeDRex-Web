var path = require('path')
var webpack = require('webpack')
// const HtmlWebpackPlugin = require("html-webpack-plugin");

let host_dev = "localhost";
//let host_prod = "nedrex-server";
let host_prod= "www.exbio.wzw.tum.de/nedrex"

let host_backend_dev = "http://"+host_dev + ":8090";
let host_backend_prod = "http://"+host_prod + ":8090";

// let host_assets_dev = host_dev+":8080";
// let host_assets_prod = host_prod+":8080";

let isProduction = (process.env.NODE_ENV === "production");
console.log("production: " + isProduction)

module.exports = {

  // outputDir: "target/dist",
  // assetsDir: "static",
  // publicPath: "/backend",


  entry: './src/main.js',
  output: {
    path: path.resolve(__dirname, './dist'),
    publicPath: '/dist/',
    filename: 'build.js'
  },
  plugins: [
    // new HtmlWebpackPlugin({
    //   title: "NeDREx",
    //   filename: "index.html",
    //   path: "./dist",
    //   template: "./index.html"
    // }),
    // new CopyWebpackPlugin([
    //   { from: './index/html', to: './dist/' }
    // ]),
    new webpack.DefinePlugin({
      HOST_BACKEND: JSON.stringify(isProduction ? host_backend_prod : host_backend_dev),
      // HOST_ASSETS: JSON.stringify(isProduction ? host_assets_prod : host_assets_dev)
    })
  ],
  module: {
    rules: [
      {
        test: /\.s(c|a)ss$/,
        use: [
          'vue-style-loader',
          'css-loader',
          {
            loader: 'sass-loader',
            // Requires sass-loader@^7.0.0
            options: {
              implementation: require('sass'),
              fiber: require('fibers'),
              indentedSyntax: true // optional
            },
            // Requires sass-loader@^8.0.0
            // options: {
            //   implementation: require('sass'),
            //   sassOptions: {
            //     fiber: require('fibers'),
            //     indentedSyntax: true // optional
            //   },
            // },
          },
        ],
      },
      {
        test: /\.css$/,
        use: [
          'vue-style-loader',
          'css-loader',
        ],
      },
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          loaders: {
            // Since sass-loader (weirdly) has SCSS as its default parse mode, we map
            // the "scss" and "sass" values for the lang attribute to the right configs here.
            // other preprocessors should work out of the box, no loader config like this necessary.
            'scss': [
              'vue-style-loader',
              'css-loader',
              'sass-loader'
            ],
            'sass': [
              'vue-style-loader',
              'css-loader',
              'sass-loader?indentedSyntax'
            ]
          }
          // other vue-loader options go here
        }
      },
      {
        test: /\.js$/,
        loader: 'babel-loader',
        exclude: /node_modules/
      },
      // {
      //   test: /\.(png|jpg|gif|svg)$/,
      //   loader: 'file-loader',
      //   options: {
      //     name: '[name].[ext]?[hash]'
      //   }
      // },
      {
        test: /\.(woff(2)?|ttf|eot|svg)(\?v=\d+\.\d+\.\d+)?$/,
        use: [
          {
            loader: 'file-loader',
            options: {
              name: '[name].[ext]',
              outputPath: 'fonts/'
            }
          }
        ]
      }
    ]
  },
  resolve: {
    alias: {
      'vue$': 'vue/dist/vue.esm.js'
    },
    extensions: ['*', '.js', '.vue', '.json']
  },

  devServer: {
    historyApiFallback: true,
    noInfo: true,
    overlay: true,
    proxy: {
      "/backend/api": {
        target: {
          host: isProduction ? host_prod : host_dev,
          protocol: 'http',
          port:8090,
        },
        changeOrigin: true,
        // port: 8090
      },
      "/backend/jobs": {
        target:{
          host: isProduction ? host_prod : host_dev,
          protocol: 'ws',
          port:8090
        },
        ws: true,
        changeOrigin: true
      }
    },
  },
  performance: {
    hints: false
  },
  devtool: '#eval-source-map'
}

// if (process.env.NODE_ENV === 'production') {
//   module.exports.devtool = '#source-map'
//   // http://vue-loader.vuejs.org/en/workflow/production.html
//   module.exports.plugins = (module.exports.plugins || []).concat([
//     new webpack.DefinePlugin({
//       'process.env': {
//         NODE_ENV: '"production"'
//       }
//     }),
//     new webpack.optimize.UglifyJsPlugin({
//       sourceMap: true,
//       compress: {
//         warnings: false
//       }
//     }),
//     new webpack.LoaderOptionsPlugin({
//       minimize: true
//     })
//   ])
// }
