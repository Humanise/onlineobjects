var photoView = {
  imageId : null,
  editable : false,
  username : null,

  $ready : function() {
    var data = hui.get.firstByClass(document.body, 'js-data');

    this.editable = data.getAttribute('data-editable') == 'true';
    this.imageId = parseInt(data.getAttribute('data-id'),10);
    this.username = data.getAttribute('data-username');
    this.width = parseInt(data.getAttribute('data-width'),10);
    this.height = parseInt(data.getAttribute('data-height'),10);
    this.sizes = JSON.parse(data.getAttribute('data-sizes'));

    this.image = hui.find('.photos_photo_image img');

    if (this.editable) {
      new oo.InlineEditor({
        element : 'editableTitle',
        name : 'titleEditor'
      });
      this._addWordBehavior();
      this._attachDrop();
    }
    this._attach();
    
    // TODO This method may be better unless we can make the <source's work better
    //this._pickBest();
    //hui.listen(window, 'resize', this._pickBest.bind(this));
    if (hui.location.getBoolean('debug')) {
      this._debug();
    }
  },
  _attach : function() {
    var img = this.image;
    hui.on(img, 'click', this._present.bind(this));
    hui.on(document, 'keydown', this._onKey.bind(this));
    var load = function() {
      hui.cls.add(hui.find('.photos_photo_image'),'is-loaded');
    };
    hui.on(img, 'load', load);
    var dummy = new Image();
    dummy.onload = load;
    dummy.src = img.currentSrc;
  },
  _pickBest : function() {
    var ratio = window.devicePixelRatio > 1 ? 2 : 1;

    var container = {width: this.image.width * ratio, height: this.image.height * ratio}
    var fitted = hui.fit({width: this.width, height: this.height}, container);
    var best = this._findBestSize(fitted);
    this.image.src = best.url;
  },
  _debug : function() {
    this._drawDebug();
    hui.listen(window, 'resize', this._drawDebug.bind(this));
  },
  _drawDebug : function() {
    if (!this.debugNode) {
      this.debugNode = hui.build('span', {parent: hui.find('.photos_photo_image'), class: 'photos_photo_debug'})
    }
    var url = this.image.currentSrc;
    var widthMatch = url.match(/width([0-9]+)/);
    var width = widthMatch && widthMatch[1];
    var heightMatch = url.match(/height([0-9]+)/);
    var height = heightMatch && heightMatch[1];
    if (!width || !height) return;
    var ratio = window.devicePixelRatio > 1 ? 2 : 1;
    var container = {width: this.image.width * ratio, height: this.image.height * ratio}
    var fitted = hui.fit({width: this.width, height: this.height}, container);
    var best = this._findBestSize(fitted);
    var msg =
      'selected: ' + width + ' x ' + height +
      ' (container: ' + container.width + ' x ' + container.height + ')' +
      ' (fitted: ' + fitted.width + ' x ' + fitted.height + ')' +
      ' (ratio: ' + Math.round((width) / fitted.width * 100) + '%) ' +
      ' (best: ' + best.width + ' x ' + best.height + ')'
    ;
    this.image.src = best.url;
    this.debugNode.innerText = msg;
    console.log(msg);
  },
  _findBestSize : function(container) {
    var best = null;
    var dist = Number.MAX_VALUE;
    var area = container.width * container.height;
    for (var i = 0; i < this.sizes.length; i++) {
      var prospect = this.sizes[i];
      var rel = Math.abs(1 - (prospect.width * prospect.height) / area);
      console.info(rel, prospect)
      if (rel < dist) {
        dist = rel;
        best = prospect;
      }
    }
    return best;
  },
  _present : function() {
    oo.presentImage({
      id: this.imageId,
      width: this.width,
      height: this.height,
      placeholder: this.image.currentSrc,
      sizes: this.sizes
    });
  },
  _onKey: function(e) {
    e = hui.event(e);
    if (e.rightKey) {
      this.next();
    } else if (e.leftKey) {
      this.previous();
    }
  },
  next : function() {
    var next = hui.find('.photos_photo_next');
    next && next.click();
  },
  previous : function() {
    var prev = hui.find('.photos_photo_previous')
    prev && prev.click();
  },
  _attachDrop : function() {
    hui.drag.listen({
      element : hui.get.firstByClass(document.body, 'photos_photo_image'),
      hoverClass : 'is-dropping',
      $dropFiles : this._dropFiles.bind(this)
    })
  },
  _dropFiles : function(files) {
    if (files.length > 0) {
      this._addFile(files[0]);      
    }
  },
  _addFile : function(file) {
    var indicator = hui.ui.ProgressIndicator.create({size: 120, opacity: .5});
    var img = hui.get.firstByClass(document.body, 'photos_photo_image');
    var container = hui.build('div.photos_photo_image_progress', {parent: img});
    container.appendChild(indicator.getElement());
    var cleanup = function() {
      hui.dom.remove(container);
      hui.ui.destroy(indicator);
    }
    hui.ui.request({
      url : '/replace',
      parameters : {id : this.imageId},
      file : file,
      $success : function() {
        document.location.reload();
      }.bind(this),
      $failure : function() {
        hui.ui.msg.fail({text: 'Unable to replace photo'});
        this._uploadEnded()
      }.bind(this),
      $progress : function(loaded,total) {
        indicator.setValue(loaded / total);
      }
    });
  },
  _addWordBehavior : function() {
    hui.listen(hui.get('words'),'click',this._onClickWord.bind(this))
  },
  $valueChanged$titleEditor : function(value) {
    hui.ui.request({
      message : {start:'Updating title', delay:300, success:'The title is changed'},
      url : '/updateTitle',
      parameters : {id:this.imageId,title:value},
      $failure : function() {
        hui.ui.msg.fail({text:'Unable to update tile'});
      }
    })
  },
  _onClickWord : function(e) {
    e = hui.event(e);
    var a = e.findByTag('a');
    if (a) {
      hui.ui.confirmOverlay({element:a,text:'Delete word?',$ok : function() {
        this._removeWord(parseInt(a.getAttribute('data'),10));
      }.bind(this)})
    }
  },
  $click$addLocation : function(widget) {
    var panel = hui.ui.get('locationPanel');
    panel.position(widget);
    panel.show()
  },
  $click$saveLocation : function() {
    var values = hui.ui.get('locationForm').getValues(),
      panel = hui.ui.get('locationPanel');

    hui.ui.request({
      message : {start:'Updating location', delay:300, success:'The location is changed'},
      url : '/updateLocation',
      json : {id : this.imageId, location : values.location},
      $success : function() {
        oo.render({id:'properties'})
      },
      $failure : function() {
        hui.ui.msg.fail({text:'Unable to update location'});
      }
    });
    panel.hide();
  },
  $click$addDescription : function(button) {
    hui.ui.get('descriptionPages').next()
    hui.ui.get('description').focus();
  },
  $click$cancelDescription : function(button) {
    hui.ui.get('descriptionPages').next()
  },
  $click$saveDescription : function(button) {
    var text = hui.ui.get('description').getValue();
    hui.ui.request({
      message : {start:'Updating description',delay:300, success:'The description is changed'},
      url : '/updateDescription',
      parameters : {id : this.imageId, description : text},
      $success : function() {
        oo.update({id:'photos_photo_description'});
      },
      $failure : function() {
        hui.ui.msg.fail({text:'Unable to update description'});
      }
    })
  },
  $click$syncMetaData : function(button) {
    button.setEnabled(false)
    hui.ui.request({
      message : {start:'Synchronizing',delay:300, success:'The meta data is synchronized'},
      url : '/synchronizeMetaData',
      parameters : {imageId : this.imageId},
      $success : function() {
        oo.update({id:'properties'});
      },
      $failure : function() {
        hui.ui.msg.fail({text:'Unable to synchronize metadata'});
      },
      $finally : function() {
        button.setEnabled(true)
      }
    })

  },

  $add$words : function(info) {
    if (!this.imageId) {
      throw 'No id';
    }
    hui.ui.request({
      message : {start:'Adding word', delay:300, success:'The word is added'},
      url : '/relateWord',
      parameters : {image : this.imageId, word : info.id},
      $success : info.callback,
      $failure : function() {
        hui.ui.msg.fail({text:'Unable to add word'});
        info.callback();
      }
    })
  },

  $delete$words : function(info) {
    hui.ui.request({
      message : {start:'Removing word', delay:300, success:'The word is removed'},
      url : '/removeWord',
      parameters : {image : this.imageId, word : info.id},
      $success : info.callback,
      $failure : function() {
        hui.ui.msg.fail({text:'Unable to remove word'});
        info.callback();
      }
    })
  },

  $valueChanged$publicAccess : function(value) {
    hui.ui.request({
      message : {start:'Changing access', delay:300, success:'Access has changed'},
      url : '/changeAccess',
      parameters : {image : this.imageId, 'public' : value},
      $failure : function() {
        hui.ui.msg.fail({text:'Unable to change access'});
      }
    })
  },

  $valueChanged$featured : function(value) {
    hui.ui.request({
      message : {start:'Changing', delay:300, success:'Changed'},
      url : '/changeFeatured',
      parameters : {image : this.imageId, 'featured' : value},
      $failure : function() {
        hui.ui.msg.fail({text: 'Unable to change highlighted'});
      }
    })
  },

  $valueChanged$theMap : function(info) {
    hui.ui.request({
      message : {start:'Changing location', delay:300, success:'The location is changed'},
      url : '/updateLocation',
      json : {id : this.imageId, location : info.location},
      $success : info.callback,
      $failure : function() {
        hui.ui.msg.fail({text:'Unable to change location'});
        info.callback();
      }
    });
  },

  $click$deletePhoto : function(info) {
    hui.ui.confirmOverlay({widget:info,text:'Delete?',$ok : function() {
      hui.ui.request({
        message : {start:'Deleting photo', delay:300, success:'The image is deleted'},
        url : '/deleteImage',
        parameters : {imageId : this.imageId},
        $failure : function() {
          hui.ui.msg.fail({text:'Unable to delete photo'});
        },
        $success : function() {
          document.location = '/'+oo.language+'/users/'+this.username+'/'
        }.bind(this)
      })
    }.bind(this)})
  },

  $click$viewMetaData : function() {
    hui.ui.request({
      url : '/getMetaData',
      parameters : {imageId : this.imageId},
      $object : function(data) {
        var html = '';
        hui.each(data,function(key,value) {
          html+='<p class="photos_photo_property"><strong>' + key + ':</strong> ' + value + '</p>';
        })
        hui.build('div',{html:html,parent:hui.get('properties')});
      }
    });
  }
};

hui.ui.listen(photoView);