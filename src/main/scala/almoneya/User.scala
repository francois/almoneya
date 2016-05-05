package almoneya

import org.joda.time.DateTime

case class User(id: Option[UserId] = None,
                tenantId: TenantId,
                surname: Surname,
                restOfName: Option[RestOfName],
                createdAt: DateTime,
                updatedAt: DateTime)
