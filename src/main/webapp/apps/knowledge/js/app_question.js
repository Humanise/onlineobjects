hui.control({
  '#name' : 'questions',
  nodes : {
    intel: '.js-question-intel',
    title: '#questionTitleText'
  },
  components: {
    answers: 'questionAnswers',
    words: 'questionWords',
    tags: 'questionTags'
  },

  show(data) {
    hui.dom.setText(this.nodes.title, data.text);
    this.components.answers.setData(data.answers);
    this.components.words.setData(data.words);
    this.components.tags.setData(data.tags);
    this.nodes.intel.style.display = 'none';
  },

  'answerQuestion.click!'() {
    var item = appController.getCurrentItem();
    var url = '/app/question/answer?id=' + item.id;
    var output = this.nodes.intel;
    output.innerText = 'Let me think...';
    output.style.display = '';
    // TODO: Handle if the questions goes away
    oo.intelligence.stream({url: url, $html: (s) => {
      output.innerHTML = s;
    }})
  }
});