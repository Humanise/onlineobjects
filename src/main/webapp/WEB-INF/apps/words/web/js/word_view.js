hui.ui.listen({
  language : null,
  text : undefined,
  activeRelation : null,
  activeRelationUrl : null,

  $ready : function() {
    this._attach();
    var id = hui.location.getHash();
    if (id!==null) {
      var node = hui.get('word-'+id);
      if (node) {
        hui.cls.add(node,'words_word_variant_highlighted')
      }
    }
    window.wordView = this;
  },

  getText : function() {
    if (this.text == undefined) {
      this.text = hui.get.firstByClass(document.body,'words_word').getAttribute('data-text');
    }
    return this.text;
  },

  _attach : function() {
    hui.listen('word','click',function(e) {
      e = hui.event(e);
      var relation = e.findByClass('words_word_relation');
      if (relation) {
        var panel = hui.ui.get('relationInfoPanel');
        if (panel) {
          e.stop();
          this.activeRelationUrl = relation.href;
          hui.ui.request({
            url : '/getRelationInfo',
            parameters : {
              relationId : relation.getAttribute('data-relation'),
              wordId : relation.getAttribute('data-word'),
              language : oo.language
            },
            $object : function(obj) {
              this.activeRelation = obj;
              var info = hui.build('div',{'class':'word_word_relation_info',html:obj.rendering});
              hui.ui.get('relationFragment').setContent(info);
              panel.position(relation);
              panel.show();
            }.bind(this)
          });
          return;
        }
      }
      var more = e.findByClass('words_word_relations_showmore');
      if (more) {
        hui.cls.toggle(more.parentNode,'words_word_relations_reveal');
      }
    }.bind(this));
  },

  $click$visitRelationButton : function() {
    document.location = this.activeRelationUrl;
  },

  $added$diagram : function() {
    var diagram = hui.ui.get('diagram');
    hui.ui.request({
      url : '/diagram.json',
      parameters : {word:this.getText()},
      $object : function(data) {
        diagram.$objectsLoaded(data);
      }
    })
  },
  $open$diagram : function(node) {
    hui.log(node);
    if (node.data) {
      if (node.data.type=='Item/Entity/Word') {
        document.location = node.data.name+'.html';
      }
    }
  }
});