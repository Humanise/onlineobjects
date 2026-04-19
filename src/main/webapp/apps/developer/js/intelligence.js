hui.control({
  $ready : function() {
    oo.intelligence.getModels().then(models => {
      var drop = hui.ui.get('model');
      drop.setItems(models.map(model => ({value: model.id, text: model.description})))
      drop.selectFirst();
    })
  },
  'form.submit!'(e) {
    var values = e.source.getValues();
    var prompt = values.prompt;
    var model = hui.ui.get('model').getValue();
    var body;
    if (values.type == 'summarize') {
      var url = '/intelligence/summarize?text=' + encodeURIComponent(prompt);
    } else {
      var url = '/intelligence/prompt/stream';
      body = {
        model: model,
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
  },
  'vectorForm.submit!'(e) {
    const values = e.source.getValues();
    const output = hui.query('#vectorOutput');
    output.text('...')
    hui.request({
      url: '/intelligence/compare',
      parameters: values,
      $success(xhr) {
        output.text(xhr.responseText);
      }
    })
  }
})