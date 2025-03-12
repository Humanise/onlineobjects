oo.intelligence = {
  _fetch : async function(params) {
    var ops = {
      method: params.method || 'GET'
    }
    if (params.form) {
      ops.headers = {
        'Content-Type': 'application/x-www-form-urlencoded'
      }
      ops.body = new URLSearchParams(params.form)
    }
    const response = await fetch(params.url, ops);
  
    // Ensure the response is a ReadableStream
    if (!response.body) {
      throw new Error('ReadableStream not supported');
    }
    const reader = response.body.getReader();
    const decoder = new TextDecoder();
  
    try {
      while (true) {
        const { done, value } = await reader.read();
      
        if (done) {
          break;
        }
        // Decode the chunk and append to UI
        const decodedChunk = decoder.decode(value, { stream: true });
      
        // Update UI with incremental content
        params.$chunk(decodedChunk)
      }
      params.$finally && params.$finally();
    } catch (error) {
      console.error('Streaming error:', error);
    }
  },
  stream : function(params) {
    var text = '';
    var started = false;
    var pre = 'Hmm ... let me think about that really carefully using all the power at my disposal'.split(' ');
    var x = [];
    var next;
    next = function() {
      if (text !== '') return;
      var word = pre.shift();
      x.push(word || '...just a second')
      var time = word ? (200 + Math.random()*200) : 2000;
      params.$html && params.$html('<p>' + x.join(' ') + '</p>');
      window.setTimeout(next, time)
    }
    window.setTimeout(next, 2000)
    
    const onChunk = (s) => {
      text += s;
      params.$html && params.$html(this.markdown(text));
    }
    
    this._fetch({
      url: params.url,
      method: params.method,
      form: params.form,
      $chunk: onChunk,
      $finally: params.$finally
    });
  },
  markdown : (str) => {
    if (window.marked) {
      return marked.parse(str);
    }
    return str.replace(/\*\*([^\*]{2,})\*\*/g, function (found) {
      found = found.substring(2,found.length - 2)
      return '<strong>' + found + '</strong>';
    })
  }
}