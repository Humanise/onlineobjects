hui.on(['hui.ui'], function() {


hui.ui.listen({
  $ready : function() {
    hui.drag.listen({
      element : document.body,
      hoverClass : 'words_dropping',
      $dropFiles : this._dropFiles.bind(this)/*,
      $dropText : this._dropText.bind(this),
      $dropURL : this._dropURL.bind(this)*/
    });
  },
  _dropFiles : function(files) {
    var win = hui.ui.Window.create({width:300});
    var upload = hui.ui.Upload.create({url:'/upload'});
    upload.listen({
      $uploadDidComplete : function(info) {
        document.location='/en/import/'+info.request.responseText+'/';
      }
    })
    win.add(upload);
    win.show();
    upload.uploadFiles(files);
  },
  $click$enrich : function() {
    document.location='/'+oo.language+'/enrich/';
  },
  $click$import : function() {
    hui.ui.get('importWindow').show();
    return;

    if (!this._importWindow) {
      var win = this._importWindow = hui.ui.Box.create({title:'Import',absolute:true,width:500,modal:true,padding:10,closable:true});
      win.addToDocument();
      var form = this._importForm = hui.ui.Formula.create({name:'importFormula'});
      form.buildGroup(null,[
        {'type':'TextInput',label:'Adresse',options:{key:'url'}}
      ]);
      win.add(form);
    }
    this._importWindow.show();
    this._importForm.setValues({url:'http://politiken.dk/rss/senestenyt.rss'}); //http://en.wikipedia.org/wiki/Language
    this._importForm.focus();
  },

  $uploadDidComplete$importUpload : function(info) {
    document.location = '/en/importlist/'+info.request.responseText+'/';
  },

  $submit$importFormula : function(form) {
    var values = form.getValues();
    var url = values.url;
    if (hui.isBlank(url)) {
      hui.ui.msg.fail({text:'The address must be filled in'});
      form.focus();
      return;
    }
    this._importWindow.hide();

    hui.ui.msg({text:'Fetching data...',busy:true});
    hui.ui.request({
      url : '/startImport',
      parameters : {url:url},
      $object : function(id) {
        hui.ui.msg({text:'Complete, redirecting...'});
        hui.defer(function() {
          document.location = '/en/import/'+id+'/';
        })
      },
      $failure : function() {
        hui.ui.msg.fail({text:'The import failed'});
      }
    })
  },

  $submit$wordsSidebarSearch : function(field) {
    var url = '/'+oo.language+'/search/?text='+field.getValue();
    document.location = url;
  }
})

})