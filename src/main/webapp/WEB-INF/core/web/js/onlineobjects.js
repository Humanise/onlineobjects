window.oo = window.oo || {};

oo.buildThumbnail = function(options) {
  var t = hui.build('span',{'class':'oo_thumbnail oo_thumbnail_frame'});
  var height = options.height;
  var width = options.width;
  if (!width && options.image) {
    width = Math.round(options.image.width/options.image.height*height);
  }
  t.style.width=width+'px';
  t.style.height=height+'px';
  if (options.variant) {
    hui.cls.add(t,'oo_thumbnail_'+options.variant);
  }
  if (options.image) {
    var img = hui.build('img',{'src':'/service/image/id'+options.image.id+'width'+width+'height'+height+'.jpg'});
    t.appendChild(img);
    if (options.zoom) {
      hui.cls.add(t,'oo_thumbnail_zoom');
      img.onclick = function() {oo.community.showImage(options.image)};
    }
  }
  return t;
};

oo.buildThumbnailHtml = function(options) {
  var cls = options.variant ? 'oo_thumbnail oo_thumbnail_frame oo_thumbnail_'+options.variant : 'oo_thumbnail';
  var html = '<span class="'+cls+'" style="width: '+options.width+'px; height: '+options.height+'px;"></span>';
  return html;
};

oo.update = function(options) {
  var id = options.id;
  var nodes = [];
  if (hui.isArray(id)) {
    for (var i=0; i < id.length; i++) {
      var nd = hui.get(id[i]);
      if (nd) {
        nodes.push(nd);
      } else {
        hui.log('Node not found : '+id[i]);
      }
    };
  } else {
    var node = hui.get(id);
    if (!node) {
      hui.log('Node not found: '+id);
    } else {
      nodes.push(node);
    }
  }
  var fades = [];
  if (options.fade) {
    for (var i=0; i < nodes.length; i++) {
      var node = nodes[i];
      var pos = hui.position.get(node);
      var hider = hui.build('div',{parent:document.body,style:{
        position : 'absolute',
        left : pos.left+'px',
        top : pos.top+'px',
        width : node.offsetWidth+'px',
        height : node.offsetHeight+'px',
        background : '#fff',
        opacity : 0,
        webkitUserSelect : 'none',
        cursor : 'wait'
      }})
      hui.animate({node:hider,css:{opacity:0.2},delay:200,ease:hui.ease.slowFastSlow,duration:300});
      fades.push(hider);
    };
  }
  hui.ui.request({
    url : document.location+'',
    headers : {'OnlineObjects-Scripts' : 'united'},
    $success : function(t) {
      var e = hui.build('div',{html:t.responseText});
      for (var i=0; i < nodes.length; i++) {
        var oldNode = nodes[i];
        hui.ui.destroyDescendants(oldNode);
        try {
          var newNode = hui.get.byId(e,oldNode.id);
          var next = hui.get.next(newNode);
          hui.dom.replaceNode(oldNode,newNode);
          hui.dom.runScripts(newNode);
          hui.log('Next node:',next);
          if (next && hui.dom.isElement(next,'script')) {
            hui.log('Running script next to element');
            hui.dom.runScripts(next);
          } else {
            hui.log('No associated script found');
          }
        } catch (e) {
          hui.log(e);
        }
      };
      if (options.onComplete) {
        options.onComplete();
      }
      if (options.$success) {
        options.$success();
      }
    },$exception : function(a,b) {
      hui.log(a);
      hui.log(b);
    },
    $finally : function() {
      hui.each(fades,function(fade) {
        hui.log(fade)
        hui.animate({node:fade,css:{opacity:'0'},duration:100,ease:hui.ease.slowFastSlow,$complete : function() {
          hui.dom.remove(fade);
        }});
      });
    }
  })
};

oo.render = function(options) {
  jsf.ajax.request(document.createElement('form'),null,{render:options.id,onSuccess:function() {
    if (options.$success) {
      options.$success();
    }
  }})
};

oo.presentImages = function(params) {
  if (hui.ui.Presentation) {
    var presentation = hui.ui.Presentation.create();
    presentation.listen({
      $getImage : oo.getImage,
      $getPreview : function(event) {
        return event.item.placeholder;
      }
    })
    presentation.show({items: params.items, index: (params.index || 0)})
    return;
  }
}

oo.getImage = function(event) {
  var sizes = event.item.sizes;
  if (sizes) {
    for (var i = 0; i < sizes.length; i++) {
      if (event.width <= sizes[i].width || event.height <= sizes[i].height) {
        return sizes[i].url;
      }
    }
    return sizes[sizes.length - 1].url
  }
  var fitted = hui.fit(event.item, {
    width: Math.floor(event.width/100) * 100,
    height: Math.floor(event.height/100) * 100
  }, {
    upscale: false
  });
  
  return '/service/image/id'+event.item.id+'width'+fitted.width+'height'+fitted.height+'sharpen1.0quality0.8.jpg';
}

oo.presentImage = function(img) {
  if (hui.ui.Presentation) {
    var presentation = hui.ui.Presentation.create();
    presentation.listen({
      $getImage : oo.getImage,
      $getPreview : function(event) {
        return event.item.placeholder;
      }
    })
    presentation.show({items:[img]})
    return;
  }
  
  if (!this.imagePresenter) {
    this.imagePresenter = oo.PhotoViewer.create();
  }
  this.imagePresenter.setImages([img]);
  this.imagePresenter.show();
};

oo.showImage = function(img) {
  var v = this.getViewer();
  v.clearImages();
  v.addImage(img);
  v.show();
};

oo.getViewer = function() {
  if (!this.imageViewer) {
    var v = this.imageViewer = hui.ui.ImageViewer.create();
    v.listen({
      $resolveImageUrl : function(image,width,height) {
        return '/service/image/?id='+image.id+'&width='+width+'&height='+height;
      }
    });
  }
  return this.imageViewer;
}

hui.ui.listen({
  $ready : function() {
    if (hui.browser.touch) {
      hui.cls.add(document.body,'oo_touch');
    }
  }
});


oo.signUp = function() {
  if (this._signUpLoading) return;
  this._signUpLoading = true;
  hui.build('script',{
    'src': '/core/js/oo_signup.js',
    parent: document.head
  });
}

oo.recover = function() {
  if (this._recoverLoading) return;
  this._recoverLoading = true;
  hui.build('script',{
    'src': '/core/js/oo_recover.js',
    parent: document.head
  });
}

hui.on(['hui'],function() {
  hui.on(hui.find('.js-signup'),'click',function(e) {
    if (!hui.ui.Panel) return;
    hui.stop(e);
    oo.signUp();
  })
});
