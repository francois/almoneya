package almoneya

case class Envelope(id: Option[EnvelopeId] = None,
                    name: EnvelopeName,
                    balance: Option[Amount] = None)
