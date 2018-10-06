hui={_:[],on:function(){this._.push(arguments)}};

window.onerror = function(msg, url, line, column) {
  try {
    var e = encodeURIComponent;
    var img = document.createElement('img');
    img.src = '/service/dependency/error?message=' + e(msg) +'&file=' + e(url) + '&line=' + e(line)'&column=' + e(column) + '&url=' + e(document.location.href)
    img.style = 'width:1px;height:1px;position:absolute;';
    document.body.appendChild(img);
  } catch (i) {console.error(i)}
};