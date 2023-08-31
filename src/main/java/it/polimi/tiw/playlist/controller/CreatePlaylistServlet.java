package it.polimi.tiw.playlist.controller;

import java.io.*;
import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import java.sql.*;
import java.util.ArrayList;

import it.polimi.tiw.playlist.dao.PlaylistDAO;
import it.polimi.tiw.playlist.dao.SongDAO;
import it.polimi.tiw.playlist.utils.ConnectionHandler;

@WebServlet("/CreatePlaylist")
@MultipartConfig
public class CreatePlaylistServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public CreatePlaylistServlet() {
		super();
	}
	
	public void init() throws ServletException{
		try {
			ServletContext context = getServletContext();
			this.connection = ConnectionHandler.getConnection(context);
			
		} catch (UnavailableException  e) {
			
		}
	}
	
	//method that creates a playlist
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException{
		HttpSession session = request.getSession(true);
		SongDAO songDAO = new SongDAO(this.connection);
		PlaylistDAO playlistDAO = new PlaylistDAO(this.connection);
		String userName = (String)session.getAttribute("user");
		String error = null;
		
		String playlistName = request.getParameter("playlistName");
		
		//checking whether the given playlist name is valid or not
		if(playlistName == null || playlistName.isEmpty()) {
			error = "Missing parameters";
		}
		else if(playlistName.length() > 50) {
			error = "Playlist name is too long";
		}
		if(error == null){
			try {
				if(playlistDAO.taken(playlistName, userName)) {
					error = playlistName + " playlist already exist";
				}
			} catch (SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
				response.getWriter().println("Database error, try again");
				return;
			}
		}
		
		//if an error occurred, it will be shown in the page
		if(error != null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400
			response.getWriter().println(error);
			return;
		}
		
		//taking the selected songs and checking whether they are valid or not
		ArrayList<Integer> songsToAdd = new ArrayList<Integer>();
		
		try {
			int maxSize = songDAO.getNumOfSongsbyUser(userName);
			for(Integer i=0; i<maxSize;i++) {
				
				String song = request.getParameter("song"+i.toString());
				if(song != null) { //This song has been chosen
					
					Integer songId = Integer.parseInt(song);
					if(songDAO.belongTo(songId, userName) ) {
						songsToAdd.add(songId);
					}
				}
			}
			if(songsToAdd.isEmpty()) {
				error = "You must select at least one song";
			}
		}
		catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
			response.getWriter().println("Database error, try again");
			return;
		}
		catch(NumberFormatException e1) {
			error = "Something went wrong";
		}
		
		//if an error occurred, it will be shown in the page
		if(error != null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400
			response.getWriter().println(error);
			return;
		}
		
		try {
			if(!playlistDAO.addPlaylistWithSongs(playlistName, userName, new Date(System.currentTimeMillis()), songsToAdd.toArray(new Integer[songsToAdd.size()]))) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400
				response.getWriter().println("Database error: unable to upload your playlist");
				return;
			}
		} catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
			response.getWriter().println("Database error, try again");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);//Code 200
	}
	
	public void destroy() {
	      ConnectionHandler.destroy(this.connection);
	}
	
}
