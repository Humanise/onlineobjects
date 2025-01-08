hui.ui.listen({

  $resolveImageUrl : function(image,width,height) {
    return hui.ui.context+'/service/image/?id='+image.id+'&width='+width+'&height='+height;
  }

})