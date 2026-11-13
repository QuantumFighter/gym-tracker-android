from pathlib import Path
base=Path(__file__).resolve().parent
source=(base/'source.html').read_text()
head='<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Gym Tracker UI Prototype</title><style>:root{color-scheme:light dark}body{margin:0;background:light-dark(#f5f6f8,#14171d)}button,summary,select{cursor:pointer}.viz-dotted-background{background-image:radial-gradient(light-dark(#cfd7e2,#343e4b) 1px,transparent 1px);background-size:16px 16px}</style></head><body>'
(base/'index.html').write_text(head+source+'</body></html>')
print(base/'index.html')
