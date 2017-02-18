var imagesController = {
  dragDrop : [
    {drag:'image',drop:'tag'}
  ],
  images : [],


  $drop$image$tag : function(dragged,dropped) {
    hui.ui.request({
      url : '/service/model/addTag',
      parameters : {id : dragged.id, tag: dropped.value},
      $success : function() {
        this.refreshAll();
      }.bind(this)
    })
  },

  refreshAll : function() {
    tagsSource.refresh();
    imagesSource.refresh();
  },

  //////////////// Image //////////////

  $itemOpened$imageGallery : function(object) {
    this.imageId = object.id;
    hui.ui.request({
      url : '/getImage',
      parameters : {id:object.id},
      $object : function(image) {
        imageFormula.setValues(image);
        imageWindow.show();
      }
    })
  },

  $click$cancelImage : function(object) {
    imageFormula.reset();
    imageWindow.hide();
  },

  $click$saveImage : function() {
    var values = imageFormula.getValues();
    var self = this;
    hui.ui.request({
      url : '/updateImage',
      parameters : {
        id : this.imageId,
        name : values.name,
        description : values.description,
        tags : values.tags
      },
      $success : function(image) {
        self.refreshAll();
        imageFormula.reset();
        imageWindow.hide();
      }
    })
  },

  $click$deleteImage : function() {
    this._deleteImage(this.imageId);
  },
  $click$deleteSelectedImage : function() {
    var obj = imageGallery.getFirstSelection();
    if (obj) {
      this._deleteImage(obj.id);
    }
  },
  _deleteImage : function(id) {
    var self = this;
    hui.ui.request({
      url : '/service/model/removeEntity',
      parameters : {id : id},
      $success : function() {
        self.refreshAll();
        imageFormula.reset();
        imageWindow.hide();
      }
    });
  },

  //////////////// Upload //////////////

  $click$newImage : function() {
    newImageWindow.show();
  },

  $uploadDidCompleteQueue : function() {
    this.refreshAll();
  },

  /////////////// Slide show ////////////

  $click$slideShow : function() {
    if (!this.viewer) {
      this.viewer = hui.ui.ImageViewer.create();
      this.viewer.listen(this);
    }
    this.viewer.clearImages();
    this.viewer.addImages(imageGallery.getObjects());
    this.viewer.show();
  },

  ///////////// Image gallery //////////

  $resolveImageUrl : function(image,width,height) {
    return hui.ui.context+'/service/image/?id='+image.id+'&width='+width+'&height='+height;
  }
}