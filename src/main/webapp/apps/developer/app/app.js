hui.control({
  components: {
    list: 'list',
    foundation: 'foundation'
  },
  list: '#list',

  'ready!'() {
    this.components.list.setData([
      {id: 0, text: 'Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. '},
      {id: 1, text: 'Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.'},
      {id: 2, text: 'Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.'},
      {id: 3, text: 'Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.'}
    ])
  },
  'list.render!'(e) {
    return hui.build('div', {text: e.value.text})
  }
})