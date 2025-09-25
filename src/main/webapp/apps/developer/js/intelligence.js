hui.ui.listen({
  $ready : function() {
    oo.intelligence.getModels().then(models => {
      var drop = hui.ui.get('model');
      drop.setItems(models.map(model => {return {value: model.id, text: model.description}}))
      drop.selectFirst();
    })
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
        model: hui.ui.get('model').getValue(),
        prompt: prompt
      }
    }
    var result = hui.find('#result');
    result.innerText = 'Lets see...';
    oo.intelligence.enableMarkdown().then(() => {
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
    })
  }
})