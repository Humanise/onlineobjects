hui.ui.listen({
  $ready : function() {
  },
  $submit$form : function(form) {
    var values = form.getValues();
    var prompt = values.prompt;
    var body;
    if (values.type == 'summarize') {
      var url = '/intelligence/summarize?text=' + encodeURIComponent(prompt);
    } else {
      var url = '/intelligence/prompt/stream';
      body = {
        prompt: prompt
      }
    }
    var result = hui.find('#result');
    result.innerText = 'Lets see...';
    oo.intelligence.stream({
      url: url, 
      method: 'POST', 
      form: body,
      $html : (str) => {
        result.innerHTML = str;
      },
      $finally: () => {
        // 
      }
    });
  }
})