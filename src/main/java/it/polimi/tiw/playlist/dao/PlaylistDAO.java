package it.polimi.tiw.playlist.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import it.polimi.tiw.playlist.beans.Album;
import it.polimi.tiw.playlist.beans.Playlist;
import it.polimi.tiw.playlist.beans.Song;
import it.polimi.tiw.playlist.utils.FromJsonToArray;

public class PlaylistDAO {
	
	private Connection con;
	
	public PlaylistDAO(Connection c) {
		this.con = c;
	}
	
	//method that creates a new playlist
	private boolean addPlaylist(String playlistName, String userName, Date creationDate) throws SQLException {
		boolean result = false;
		String query = "INSERT into PLAYLIST (Name , UserName, CreationDate) VALUES (? , ? , ?)";
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1 , playlistName);
			pStatement.setString(2, userName);
			pStatement.setDate(3 , creationDate);
			if(!this.taken(playlistName, userName)) {
				pStatement.executeUpdate();
				result = true;
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if (pStatement != null) {
					pStatement.close();
				}
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		return result;
	}
	
	//method that states whether a song is in a playlist or not
	private boolean songAlreadyIn(String playlistName, String userName, int songId) throws SQLException {
		boolean result = false;
		String query = "SELECT * FROM CONTAINS WHERE PlaylistName = ? AND PlaylistUser = ? AND Song = ?";
		
		PreparedStatement pStatement = null;
		ResultSet queryRes = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, playlistName);
			pStatement.setString(2, userName);
			pStatement.setInt(3, songId);
			
			queryRes = pStatement.executeQuery();
			
			if(queryRes.next())
				result = true;
		}catch(SQLException e) {
			throw new SQLException();
		}finally{
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
		    }
		}
		return result;
	}
	
	//method that adds the given song to the given playlist
	public boolean addSongToPlaylist(String playlistName, String userName, int songId) throws SQLException {
		boolean result = false;
		String query = "INSERT into CONTAINS (PlaylistName, PlaylistUser, Song) VALUES (? , ? , ?)";
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, playlistName);
			pStatement.setString(2, userName);
			pStatement.setInt(3, songId);
			if(!this.songAlreadyIn(playlistName, userName, songId)) {
				pStatement.executeUpdate();
				result = true;
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
		    }
		}
		return result;
	}
	
	//methods that creates a playlist wich contains some songs
	public boolean addPlaylistWithSongs(String playlistName, String userName, Date creationDate, Integer[] songs) throws SQLException {
		if(songs.length == 0) return false;
		boolean result = false;
		
		try {
			con.setAutoCommit(false);
			
			if(this.addPlaylist(playlistName, userName, creationDate)) {
				boolean flag = true;
				for(int i=0; i<songs.length && flag;i++) {
					if(!this.addSongToPlaylist(playlistName, userName, songs[i])) {
						flag = false;
					}
				}
				if(flag) {
					con.commit();
					result = true;
				}
				else con.rollback();
			}
		}catch(SQLException e){
			con.rollback();
			throw e;
		}finally {
			con.setAutoCommit(true);
		}
		return result;
	}
	
	//method that verifies whether a playlist belongs to the given user or not
	public boolean belongTo(String playlistName, String userName) throws SQLException{
		boolean result = false;
		String query = "SELECT * FROM PLAYLIST WHERE Name = ? AND UserName = ?";
		PreparedStatement pStatement = null;
		ResultSet queryRes = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, playlistName);
			pStatement.setString(2, userName);
			
			queryRes = pStatement.executeQuery();
			
			if(queryRes.next()) {
				result = true;
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}	
		return result;
	}	

	// method that returns all playlists related to a User
	public ArrayList<Playlist> allPlaylists(String userName) throws SQLException {
		ArrayList<Playlist> result = new ArrayList<Playlist>();
		String query = "SELECT * FROM PLAYLIST WHERE UserName = ? ORDER BY CreationDate DESC";
		ResultSet queryRes = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1 , userName);
			
			queryRes = pStatement.executeQuery();
			
			while(queryRes.next()) {
				result.add( new Playlist(queryRes.getString("Name") , queryRes.getDate("CreationDate")) );
			}
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		
		return result;
	}
	
	//method that states whether a playlist name is already taken or not
	public boolean taken(String playlistName, String userName) throws SQLException {
		boolean result = false;
		String query = "SELECT * FROM PLAYLIST WHERE Name = ? AND UserName = ?";
		ResultSet queryRes = null;
		PreparedStatement pStatement = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, playlistName);
			pStatement.setString(2, userName);
			queryRes = pStatement.executeQuery();
			
			if(queryRes.next()) result = true;
			
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
		    }
		}
		return result;
	}

	//method that edits the sorting of a playlist
	public boolean editSorting(String jsonSorting, String playlistName, String userName) throws SQLException{
		boolean result = false;
		String query = "UPDATE PLAYLIST SET Sorting = ? WHERE Name = ? AND UserName = ?";
		PreparedStatement pStatement = null;
		
		try{
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, jsonSorting);
			pStatement.setString(2, playlistName);
			pStatement.setString(3, userName);
			if(pStatement.executeUpdate() > 0) result = true;
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
		    }
		}
		
		return result;
	}
	
	//method that returns the sorting of the given playlist
	private ArrayList<Integer> getSorting(String playlistName, String userName) throws SQLException{
		ArrayList<Integer> result = null;
		String query = "SELECT Sorting FROM PLAYLIST WHERE Name = ? AND UserName = ?";
		PreparedStatement pStatement = null;
		ResultSet queryRes = null;
		String jSon = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, playlistName);
			pStatement.setString(2, userName);
			
			queryRes = pStatement.executeQuery();
			
			if(queryRes.next())
				 jSon = queryRes.getString("Sorting");
			
			if(jSon == null)
				return null;
			
			result = FromJsonToArray.fromJsonToArrayList(jSon);
			
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
		    }
		}
		
		return result;
	}
	
	//method that returns the necessary attributes for the presentation of the playlist 
	public ArrayList<Song> getSongTitleAndImg(String playlistName, String userName) throws SQLException{
		ArrayList<Song> result = new ArrayList<Song>();
		ArrayList<Song> temp = new ArrayList<Song>();
		String query = "SELECT SONG.Id, SONG.Title, ALBUM.FileImage "
				+ "FROM CONTAINS JOIN SONG ON CONTAINS.Song = SONG.Id JOIN ALBUM ON SONG.Album = ALBUM.Id "
				+ "WHERE CONTAINS.PlaylistName = ? AND CONTAINS.PlaylistUser = ? ORDER BY ALBUM.PublicationYear DESC";
		PreparedStatement pStatement = null;
		ResultSet queryRes = null;
		
		try {
			pStatement = con.prepareStatement(query);
			pStatement.setString(1, playlistName);
			pStatement.setString(2, userName);
			
			queryRes = pStatement.executeQuery();
			
			Album album;
			Song song;
			//prepare temp with songs ordered by album publication year desc
			while(queryRes.next()) {
				album = new Album();
				song = new Song();
				
				//Read the image from the data base
				song.setId(queryRes.getInt("SONG.Id"));
				song.setTitle(queryRes.getString("SONG.Title"));
				album.setFileImage(queryRes.getString("ALBUM.FileImage"));
				song.setAlbum(album);
				temp.add(song);
			}
			
			ArrayList<Integer> sorting = getSorting(playlistName,userName);
			if(sorting == null) result = temp;
			else {
				for(Integer i : sorting) {
					for(Song s : temp) {
						if(s.getId() == i) result.add(s);
					}
				}
				
				for(Song s : temp) {
					if( !result.contains(s) ) result.add(s);
				}
			}
			
		}catch(SQLException e) {
			throw new SQLException();
		}finally {
			try {
				if(queryRes != null) {
					queryRes.close();
				}
			}catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				if(pStatement != null) {
					pStatement.close();
				}
			}catch(Exception e2) {
				throw new SQLException(e2);
			}
		}
		return result;
	}
		
}