hui.ui.listen({
  $ready : function() {
  },
  $submit$form : function(form) {
    var values = form.getValues();
    var prompt = values.prompt;
    if (values.type == 'summarize') {
      var url = '/intelligence/prompt/stream?prompt=' + encodeURIComponent(prompt);
    } else {
      var url = '/intelligence/summarize?text=' + encodeURIComponent(prompt);
    }
    var result = hui.find('#result');
    result.innerText = 'Lets see...';
    oo.intelligence.stream({url: url, onHtml : (str) => {
      result.innerHTML = str;
    }, onEnd: () => {
      // 
    }});
  }
})