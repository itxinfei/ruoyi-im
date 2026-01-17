module.exports = {
  presets: ['@vue/cli-plugin-babel/preset'],
  plugins: [
    [
      'component',
      {
        libraryName: 'element-ui',
        styleLibrary: {
          name: 'theme-chalk',
        },
      },
    ],
  ],
}
