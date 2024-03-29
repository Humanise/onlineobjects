hui.on(function() {
  //return;
  var logo = hui.get('logo');
  var size = Math.min(logo.clientWidth, logo.clientHeight);
  var done = false;

  if (hui.browser.msie6 || hui.browser.msie7 || hui.browser.msie8) {
    return;
  }

  logo.innerHTML = '';

  var d = hui.ui.Drawing.create({width:size,height:size,parent:logo});
  var center = {
    x : size / 2,
    y : size / 2
  };
  var circleSize = 0.155;
  var innerArcSize = 0.34;


  var arcSkew = 0.045;//0.005;
  var arc1 = d.addArc({center : center,fill : 'rgba(255,255,255,.7)', stroke:{color:'#888',width : '1.5px' }, skew: arcSkew});
  var arc2 = d.addArc({center : center,fill : 'rgba(255,255,255,.7)', stroke:{color:'#888',width : '1.5px' }, skew: arcSkew});
  var arc3 = d.addArc({center : center,fill : 'rgba(255,255,255,.7)', stroke:{color:'#888',width : '1.5px' }, skew: arcSkew});
  var arc4 = d.addArc({center : center,fill : 'rgba(255,255,255,.7)', stroke:{color:'#888',width : '1.5px' }, skew: arcSkew});
  var arcs = [arc1,arc2,arc3,arc4];
  var circle = d.addCircle({cx:center.x,cy:center.y,r:0,fill : 'none', stroke:{color:'#888',width : '1.5px'},width:0});

  hui.on(window,'resize',function() {
    hui.onDraw(function() {
      var newSize = Math.min(logo.clientWidth, logo.clientHeight);
      if (newSize !== size) {
        size = newSize;
        d.setSize(size,size);
        center = {
          x : size / 2,
          y : size / 2
        };
        circle.setCenter(center);
        arc1.update({center: center})
        arc2.update({center: center})
        arc3.update({center: center})
        arc4.update({center: center})
        if (done) {
          circle.setRadius(size * circleSize);
          for (var i = 0; i < arcs.length; i++) {
            arcs[i].update({
              innerRadius : (size * innerArcSize) + 1 - 1 * (size * 0.15),
              outerRadius : ((size * innerArcSize) - 1 * (size * 0.15) + 1 * (size * 0.2)) * (i == 1 || i == 3 ? 1.1 : 1)
            })
          }
        }
      }
    })
  });


  var title = hui.get('title');
  var text = hui.dom.getText(title);
  hui.dom.clear(title);
  var chars = [];
  for (var i=0; i < text.length; i++) {
    chars.push(hui.build('span',{
      text : text[i],
      parent : title,
      style : {
        'opacity' : 0,
        'display' : 'inline-block',
        'transform' : 'scale(0)',
        'position' : 'relative',
        'top' : Math.round((Math.random() * 100 - 50)) + 'px'
      }
    }));
  };

  //var slogan = hui.get('slogan');

  //hui.cls.add(slogan, 'is-hidden');

  window.setTimeout(function() {
    window.setTimeout(animateTitle, 100);



    //window.setTimeout(function() {
    //  hui.cls.remove(slogan, 'is-hidden');
    //}, 4000);


    function animateTitle() {
      for (var i=0; i < chars.length; i++) {
        var delay = Math.random() * 500;
        var chr = chars[i];
        hui.animate({
          node : chr,
          css : {
            opacity : 1, top : '0px', transform: 'scale(1)'
          },
          delay : 500 + delay,
          ease : hui.ease.elastic,
          duration: 2000 - delay
        });

      };
    }

    var skew = 30;

    function startCircle(shape) {
      hui.animate({ node : shape,
        delay: 2000,
        duration: 2000,
        ease : hui.ease.elastic,
        $render : function(shape,pos) {
          shape.setRadius(pos * size * circleSize)
        },
        $complete : function() {
          loopCircle(shape);
        }
      })
    }

    var loopCircle = function(shape) {
      var x = Math.random() * size/-30;
      hui.animate({ node : shape,
        //delay : 2000 * Math.random(),
        duration: 2000 + 2000 * Math.random(),
        ease : hui.ease.slowFastSlow,
        $render : function(shape,pos) {
          shape.setRadius(size * circleSize + Math.sin(pos * Math.PI) * x)
        },
        $complete : function() { loopCircle(shape);}
      })
    }

    function startArc(arc,start,end,extra) {
      var ran = Math.random() * 3000;
      var turns = Math.round(Math.random() * 3) + 1;
      hui.animate({ node : arc,
        duration : 8000 - ran,
        delay : ran,
        ease : hui.ease.elastic,
        $render : function(obj,pos) {
          obj.update({
            startDegrees : (start + 3) * pos + 360 * turns * pos + skew,
            endDegrees : (end +- 3) + 360 * turns * pos + skew,
            innerRadius : (size*innerArcSize) + 1 - pos * (size*0.15),
            outerRadius : ((size*innerArcSize) - pos * (size*0.15) + pos * (size*0.2)) * extra
          })
        },
        $complete : function() {
          loopArc(arc,start,end,extra);
        }
      })
    }


    var loopArc = function(arc,start,end,extra) {
      var dir = Math.random() > .5 ? 1 : -1;
      var turns = Math.random() > .5 ? 1 : 2;
      var delay = Math.random() * 500;
      var speed = Math.random() / turns;
      var dur = (Math.round(speed * 3) + 1) * 1500;
      var radiusBulge = (speed - .6) / 2;
      var innerBulge = (speed - .2) / -2;
      hui.animate({ node : arc,
        delay : delay,
        duration : dur - delay,
        ease : hui.ease.cubicInOut,//hui.ease.backInOut,
        $render : function(node,pos) {
          arc.update({
            startDegrees : start+3 + 360 * pos * turns * dir + skew,
            endDegrees : end - 3 + 360 * pos * turns * dir + skew + 30 * turns * Math.sin(pos * Math.PI),
            innerRadius : (size*innerArcSize) + 1 - (size*0.15) * (1 + Math.sin(pos * Math.PI) * innerBulge),
            outerRadius : (size * innerArcSize - size * 0.15 + size * 0.2) * extra * (1 + Math.sin(pos * Math.PI) * radiusBulge)
          })
        },
        $complete : function() { loopArc(arc,start,end,extra);}
      })
    }

    startArc(arc1,-90,0,1)
    startArc(arc2,0,90,1.1)
    startArc(arc3,90,180,1)
    startArc(arc4,180,270,1.1)

    startCircle(circle)

    window.setTimeout(function() {
      loopCircle = function(){};
      loopArc = function(){};
      done = true;
    },80000)

  },200);

  function isMobile() {
    return (function(a){
      return (/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows (ce|phone)|xda|xiino/i.test(a) || /1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4)));})(navigator.userAgent||navigator.vendor||window.opera);
  }
})