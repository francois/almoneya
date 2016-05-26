package almoneya

case class User(id: Option[UserId] = None,
                tenantId: TenantId,
                surname: Surname,
                restOfName: Option[RestOfName])
