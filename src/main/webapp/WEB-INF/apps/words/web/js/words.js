hui.on(['hui.ui'], function() {

  hui.ui.listen({
    $submit$wordsSidebarSearch : function(field) {
      var url = '/'+oo.language+'/search/?text='+field.getValue();
      document.location = url;
    }
  })

})