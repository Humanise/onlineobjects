(function() {

  var huml = `# Hep hey!

jksfk fjksd fjdks fjkj fdsl

* This is a point
*  j asfjsakfak jflka

[ ] Check this out
[x] Do this at some point @jonas #todo
  `
  

  hui.control({
    $ready : function() {
      hui.ui.get('code').setValue(huml);
      this['code.valueChanged!'](huml);
      hui.ui.get('list').setData([{title: 'This is a test'}])
    },
    list : '@list',
    nodes: {
      rendering: '#rendering',
      json: '#json'
    },
    'list.render!' : function(obj) {
      return hui.build('span', {text: obj.title});
    },
    'list.select!' : function(e) {
      console.log(e.data);
    },
    'code.valueChanged!' : function(str) {
      let doc = new DocumentParser().parse(str);
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