-- Switch the database to write-ahead logging.
--
-- The default rollback journal takes an exclusive lock on the whole database for every write, which
-- the three queue workers collide on immediately. Write-ahead logging lets readers carry on while
-- one worker writes.
--
-- The setting is stored in the database file rather than on the connection, so running it once here
-- covers every later connection. It gets its own migration because SQLite refuses to change journal
-- mode inside a transaction, and the schema migration should stay atomic.

PRAGMA journal_mode = WAL;
