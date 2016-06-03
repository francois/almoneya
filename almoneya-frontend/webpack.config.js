const path = require('path')
const process = require("process")

const nodeEnv = process.env.NODE_ENV || 'development'
const isProd = nodeEnv === 'production'

module.exports = {
  devtool: isProd ? 'hidden-source-map' : 'cheap-eval-source-map',
  entry: {
    accounts_index: path.join(__dirname, "assets", "accounts-index-app.jsx"),
  },
  output: {
    path: path.join(__dirname, "public", "assets"),
    filename: "[name].bundle.js"
  },
  module: {
    loaders: [
      { test: /\.css$/, loader: "style!css" },
      {
        test: /\.jsx?$/,
        exclude: /(node_modules|bower_components)/,
        loader: "babel",
        query: {
          cacheDirectory: true,
          presets: ["es2015"]
        }
      }
    ]
  },
  watchOptions: {
    poll: process.platform === 'linux' ? 200 : false
  }
}
