oo.intelligence = {
  fetch : async function(url, onText, onEnd) {
    const response = await fetch(url);
  
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
        onText(decodedChunk)
      }
      onEnd && onEnd();
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
    this.fetch(params.url, (s) => {
      text += s;
      params.$html && params.$html(this.markdown(text));
    }, params.$finally);
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