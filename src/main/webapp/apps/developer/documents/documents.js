(function() {

  var huml = `# Humle

HUManise Markleft LanguagE

jksfk fjksd fjdks fjkj fdsl

* This is a point
*  j asfjsakfak jflka

[ ] Check this out
[x] Do this at some point @jonas #todo
  `
  

  hui.control({
    'ready!' : function() {
      this.components.list.setData([{title: 'This is a test'}])
      this.components.code.setValue(huml);
      this['code.valueChanged!']({value: huml});
    },
    list : '@list', // TODO: possible syntax
    rendering: '#rendering', // TODO: possible syntax
    
    components : {
      list: 'list',
      code: 'code'
    },
    nodes: {
      rendering: '#rendering',
      json: '#json'
    },
    'list.render!' : function(e) {
      return hui.build('span', {text: e.value.title});
    },
    'list.select!' : function(e) {
      console.log(e.value);
    },
    'code.valueChanged!' : function(e) {
      let doc = new DocumentParser().parse(e.value);
      let rnd = new DocumentRenderer();
      this.nodes.rendering.innerHTML = rnd.toHTML(doc);
      this.nodes.json.innerHTML = rnd.toJSON(doc);
      console.log(rnd.toHTML(doc))
    },


    _load : function() {
      hui.ui.request({
        url: '/settings/data',
        method: 'GET',
        $object : this._setValues.bind(this)
      })
    },
  })

})();