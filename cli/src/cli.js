import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
.mode('connect <username>')
.delimiter(cli.chalk['green']('connected>'))
.init(function (args, callback) {
  username = args.username
  server = connect({ host: 'localhost', port: 8080 }, () => {
    server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
    callback()
  })

  server.on('data', (buffer) => {
    this.log(Message.fromJSON(buffer).toString())
    if (Message.fromJSON(buffer).command === 'connect' || Message.fromJSON(buffer).command === 'disconnect') {
      this.log(cli.chalk['red'](Message.fromJSON(buffer).toString()))
    } else if (Message.fromJSON(buffer).command === 'echo') {
      this.log(cli.chalk['blue'](Message.fromJSON(buffer).toString()))
    } else if (Message.fromJSON(buffer).command === 'broadcast') {
      this.log(cli.chalk['magenta'](Message.fromJSON(buffer).toString()))
    } else if (Message.fromJSON(buffer).command === 'users') {
      this.log(cli.chalk['cyan'](Message.fromJSON(buffer).toString()))
    } else if (Message.fromJSON(buffer).command === 'directMessage') {
      this.log(cli.chalk['bgRed'](Message.fromJSON(buffer).toString()))
    }
  })

  server.on('end', () => {
    cli.exec('exit')
  })
})
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input)
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else if (command === 'echo') {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    } else {
      this.log(`Command <${command}> was not recognized`)
    }

    callback()
  })
