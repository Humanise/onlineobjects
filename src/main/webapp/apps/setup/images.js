hui.ui.listen({

  $resolveImageUrl : function(image,width,height) {
    return '/service/image/?id='+image.id+'&width='+width+'&height='+height;
  }

})