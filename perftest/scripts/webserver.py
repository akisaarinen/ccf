from BaseHTTPServer import BaseHTTPRequestHandler, HTTPServer

class Handler(BaseHTTPRequestHandler):
  def do(self):
    self.send_response(200)
    self.wfile.write('{"headers":{"type":"type"},"content":{"b":2}}')

  def do_GET(self):
    self.do()

  def do_POST(self):
    self.do()

def main():
  try:
    server = HTTPServer(('', 8080), Handler)
    print 'started httpserver...'
    server.serve_forever()
  except KeyboardInterrupt:
    print '^C received, shutting down server'
    server.socket.close()

if __name__ == '__main__':
  main()

