var photosLayoutView = {
  $click$addImageGallery : function() {
    hui.ui.request({
      messages : {start:'Creating gallery'},
      url : '/createGallery',
      $object : function(gallery) {
        hui.ui.msg.success({text:'Gallery created'});
        window.setTimeout(function() {
          document.location = '/'+oo.language+'/gallery/'+gallery.id+'/';
        },1000);
      }.bind(this),
      $failure : function() {
        hui.ui.msg.fail({text:'Could not create gallery'});
      }
    });

  }
}
hui.ui.listen(photosLayoutView);