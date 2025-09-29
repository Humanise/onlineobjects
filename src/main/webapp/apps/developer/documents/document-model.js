class DocumentParser {
  parse(str) {
    var doc = new Document();
    var current = null;
    let listener = {
      startHeading : () => {
        current = {type: 'heading'}
        doc.children.push(current)
      },
      startText : () => {
        current = {type: 'text'}
        doc.children.push(current)
      },
      startListItem : () => {
        if (!current || !(current instanceof List)) {
          current = new List();
          doc.children.push(current)
        } else if (current instanceof List) {
          current.addItem();
        }
      },
      startTask : (char) => {
        current = Task.fromChar(char)
        doc.children.push(current)
      },
      char : (c) => {
        if (current) {
          if (current.appendText) {
            current.appendText(c);
          } else {
            current.text = current.text || '';
            current.text += c;
          }
        }
      }
    }
    var scanner = new Scanner(str);
    var first = true
    while (scanner.hasNext()) {
      var found;
      if (first) {
        if (found = scanner.match(/^\[[ \-x]\]/)) {
          console.log(found)
          listener.startTask(found[1]);
          first = false
        } else {
          found = scanner.next();
          if (found == '#') {
            listener.startHeading();
            first = false
          } else if (found == '*') {
            listener.startListItem();
            first = false
          } else if (found == '\n') {
            // Ignore
          } else {
            listener.startText(found);
            listener.char(found);
            first = false
          }
        }
      } else {
        found = scanner.next();
        if (found == '\n') {
          first = true;
        } else {
          listener.char(found);
        }
      }      
    }
    
    return doc;
  }
}

class Document {
  children = []
}

class Part {
  text = ''
}

class Heading extends Part {
  
}

class List extends Part {
  items = []
  
  constructor() {
    super()
    this.addItem()
  }

  addItem() {
    this.items.push(new ListItem())
  }
  
  appendText(s) {
    this.items[this.items.length - 1].text += s
  }
  
  toHTML() {
    return '<ul>' + this.items.map((i) => i.toHTML()).join('') + '</ul>'
  }
}

class ListItem {
  text = ''
  
  toHTML() {
    return '<li>' + this.text + '</li>'
  }
}

class Task extends Part {
  type = 'task'
  status = null

  static fromChar(c) {
    let task = new Task();
    if (c === 'x') {
      task.status = 'done'
    } else if (c === '-') {
      task.status = 'in-progress'
    }
    return task;
  }
  
  toHTML() {
    return '<div><input type="checkbox"' + (this.status == 'done' ? ' checked' : '') + '/>' + this.text + '</div>'
  }
}

class DocumentRenderer {
  toHTML(doc) {
    var html = '';
    for (var i = 0; i < doc.children.length; i++) {
      var child = doc.children[i];
      if (child.toHTML) {
        html += child.toHTML();
      }
      else if (child.type == 'heading') {
        html += '<h1>' + child.text + '</h1>';
      }
      else if (child.type == 'text') {
        html += '<p>' + child.text + '</p>';
      }
      else if (child.type == 'list') {
        html += '<ul><li>' + child.text + '</li></ul>';
      }
    }
    return html
  }

  toJSON(doc) {
    return JSON.stringify(doc, null, 2)
  }
}

class Scanner {
  constructor(str) {
    this.str = str;
    this.pos = 0;
  }
  
  next() {
    var c = this.str[this.pos];
    if (c !== undefined) {
      this.pos++
      return c;
    }
  }

  match(rgx) {
    var str = this.str.slice(this.pos)
    var found = str.match(rgx)
    if (found) {
      var out = found[0];
      this.pos += out.length;
      return out;
    }
  }

  hasNext() {
    return this.pos < this.str.length
  }
}

if (typeof exports !== 'undefined') exports.DocumentParser = DocumentParser;