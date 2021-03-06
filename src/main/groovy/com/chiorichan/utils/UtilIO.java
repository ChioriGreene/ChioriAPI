/**
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 * Copyright (c) 2017 Joel Greene <joel.greene@penoaks.com>
 * Copyright (c) 2017 Penoaks Publishing LLC <development@penoaks.com>
 *
 * All Rights Reserved.
 */
package com.chiorichan.utils;

import com.chiorichan.AppConfig;
import com.chiorichan.AppController;
import com.chiorichan.AppLoader;
import com.chiorichan.lang.EnumColor;
import com.chiorichan.lang.ReportingLevel;
import com.chiorichan.lang.UncaughtException;
import com.chiorichan.libraries.Libraries;
import com.chiorichan.logger.Log;
import com.chiorichan.plugin.PluginManager;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class UtilIO
{
	public static final String PATH_SEPERATOR = File.separator;
	private static final int EOF = -1;
	private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

	private UtilIO()
	{

	}

	public static void closeQuietly( Closeable closeable )
	{
		try
		{
			if ( closeable != null )
				closeable.close();
		}
		catch ( IOException ioe )
		{
			// ignore
		}
	}

	public static int copy( InputStream input, OutputStream output ) throws IOException
	{
		long count = copyLarge( input, output );
		if ( count > Integer.MAX_VALUE )
			return -1;
		return ( int ) count;
	}

	public static long copyLarge( InputStream input, OutputStream output ) throws IOException
	{
		return copyLarge( input, output, new byte[DEFAULT_BUFFER_SIZE] );
	}

	public static long copyLarge( InputStream input, OutputStream output, byte[] buffer ) throws IOException
	{
		long count = 0;
		int n = 0;
		while ( EOF != ( n = input.read( buffer ) ) )
		{
			output.write( buffer, 0, n );
			count += n;
		}
		return count;
	}

	public static String bytesToStringUTFNIO( byte[] bytes )
	{
		if ( bytes == null )
			return null;

		CharBuffer cBuffer = ByteBuffer.wrap( bytes ).asCharBuffer();
		return cBuffer.toString();
	}

	public static File buildFile( boolean absolute, String... args )
	{
		return new File( ( absolute ? PATH_SEPERATOR : "" ) + buildPath( args ) );
	}

	public static File buildFile( File file, String... args )
	{
		return new File( file, buildPath( args ) );
	}

	public static File buildFile( String... args )
	{
		return buildFile( false, args );
	}

	public static String buildPath( String... path )
	{
		StringBuilder builder = new StringBuilder();

		boolean first = true;

		for ( String node : path )
		{
			if ( node.isEmpty() )
				continue;

			if ( !first )
				builder.append( PATH_SEPERATOR );

			builder.append( node );

			first = false;
		}

		return builder.toString();
	}

	public static boolean checkMd5( File file, String expectedMd5 )
	{
		if ( expectedMd5 == null || file == null || !file.exists() )
			return false;

		String md5 = md5( file );
		return md5 != null && md5.equals( expectedMd5 );
	}

	/**
	 * This method copies one file to another location
	 *
	 * @param inFile  the source filename
	 * @param outFile the target filename
	 * @return true on success
	 */
	@SuppressWarnings( "resource" )
	public static boolean copy( File inFile, File outFile )
	{
		if ( !inFile.exists() )
			return false;

		FileChannel in = null;
		FileChannel out = null;

		try
		{
			in = new FileInputStream( inFile ).getChannel();
			out = new FileOutputStream( outFile ).getChannel();

			long pos = 0;
			long size = in.size();

			while ( pos < size )
				pos += in.transferTo( pos, 10 * 1024 * 1024, out );
		}
		catch ( IOException ioe )
		{
			return false;
		}
		finally
		{
			try
			{
				if ( in != null )
					in.close();
				if ( out != null )
					out.close();
			}
			catch ( IOException ioe )
			{
				return false;
			}
		}

		return true;
	}

	public static void extractLibraries( File jarFile, File baseDir )
	{
		try
		{
			baseDir = new File( baseDir, "libraries" );
			// FileFunc.directoryHealthCheck( baseDir );

			if ( jarFile == null || !jarFile.exists() || !jarFile.getName().endsWith( ".jar" ) )
				PluginManager.getLogger().severe( "There was a problem with the provided jar file, it was either null, not existent or did not end with jar." );

			JarFile jar = new JarFile( jarFile );

			try
			{
				ZipEntry libDir = jar.getEntry( "libraries" );

				if ( libDir != null ) // && libDir.isDirectory() )
				{
					Enumeration<JarEntry> entries = jar.entries();
					while ( entries.hasMoreElements() )
					{
						JarEntry entry = entries.nextElement();
						if ( entry.getName().startsWith( libDir.getName() ) && !entry.isDirectory() && entry.getName().endsWith( ".jar" ) )
						{
							File lib = new File( baseDir, entry.getName().substring( libDir.getName().length() + 1 ) );

							if ( !lib.exists() )
							{
								lib.getParentFile().mkdirs();
								PluginManager.getLogger().info( EnumColor.GOLD + "Extracting bundled library '" + entry.getName() + "' to '" + lib.getAbsolutePath() + "'." );
								InputStream is = jar.getInputStream( entry );
								FileOutputStream out = new FileOutputStream( lib );
								ByteStreams.copy( is, out );
								is.close();
								out.close();
							}

							Libraries.loadLibrary( lib );
						}
					}
				}
			}
			finally
			{
				jar.close();
			}
		}
		catch ( Throwable t )
		{
			PluginManager.getLogger().severe( "We had a problem extracting bundled libraries from jar file '" + jarFile.getAbsolutePath() + "'", t );
		}
	}

	public static boolean extractNatives( File libFile, File baseDir ) throws IOException
	{
		List<String> nativesExtracted = new ArrayList<>();
		boolean foundArchMatchingNative = false;

		baseDir = new File( baseDir, "natives" );
		// FileFunc.directoryHealthCheck( baseDir );

		if ( libFile == null || !libFile.exists() || !libFile.getName().endsWith( ".jar" ) )
			throw new IOException( "There was a problem with the provided jar file, it was either null, not existent or did not end with jar." );

		JarFile jar = new JarFile( libFile );
		Enumeration<JarEntry> entries = jar.entries();

		while ( entries.hasMoreElements() )
		{
			JarEntry entry = entries.nextElement();

			if ( !entry.isDirectory() && ( entry.getName().endsWith( ".so" ) || entry.getName().endsWith( ".dll" ) || entry.getName().endsWith( ".jnilib" ) || entry.getName().endsWith( ".dylib" ) ) )
				try
				{
					File internal = new File( entry.getName() );
					String newName = internal.getName();

					String os = System.getProperty( "os.name" );
					if ( os.contains( " " ) )
						os = os.substring( 0, os.indexOf( " " ) );
					os = os.replaceAll( "\\W", "" );
					os = os.toLowerCase();

					String parent = internal.getParentFile().getName();

					if ( parent.startsWith( os ) || parent.startsWith( "windows" ) || parent.startsWith( "linux" ) || parent.startsWith( "darwin" ) || parent.startsWith( "osx" ) || parent.startsWith( "solaris" ) || parent.startsWith( "cygwin" ) || parent.startsWith( "mingw" ) || parent.startsWith( "msys" ) )
						newName = parent + "/" + newName;

					if ( Arrays.asList( OSInfo.NATIVE_SEARCH_PATHS ).contains( parent ) )
						foundArchMatchingNative = true;

					File lib = new File( baseDir, newName );

					if ( lib.exists() && nativesExtracted.contains( lib.getAbsolutePath() ) )
						PluginManager.getLogger().warning( EnumColor.GOLD + "We detected more than one file with the destination '" + lib.getAbsolutePath() + "', if these files from for different architectures, you might need to separate them into their separate folders, i.e., windows, linux-x86, linux-x86_64, etc." );

					if ( !lib.exists() )
					{
						lib.getParentFile().mkdirs();
						PluginManager.getLogger().info( EnumColor.GOLD + "Extracting native library '" + entry.getName() + "' to '" + lib.getAbsolutePath() + "'." );
						InputStream is = jar.getInputStream( entry );
						FileOutputStream out = new FileOutputStream( lib );
						byte[] buf = new byte[0x1000];
						while ( true )
						{
							int r = is.read( buf );
							if ( r == -1 )
								break;
							out.write( buf, 0, r );
						}
						is.close();
						out.close();
					}

					if ( !nativesExtracted.contains( lib.getAbsolutePath() ) )
						nativesExtracted.add( lib.getAbsolutePath() );
				}
				catch ( FileNotFoundException e )
				{
					jar.close();
					throw new IOException( "We had a problem extracting native library '" + entry.getName() + "' from jar file '" + libFile.getAbsolutePath() + "'", e );
				}
		}

		jar.close();

		if ( nativesExtracted.size() > 0 )
		{
			if ( !foundArchMatchingNative )
				PluginManager.getLogger().warning( EnumColor.DARK_GRAY + "We found native libraries contained within jar '" + libFile.getAbsolutePath() + "' but according to conventions none of them had the required architecture, the dependency may fail to load the required native if our theory is correct." );

			String path = baseDir.getAbsolutePath().contains( " " ) ? "\"" + baseDir.getAbsolutePath() + "\"" : baseDir.getAbsolutePath();
			System.setProperty( "java.library.path", System.getProperty( "java.library.path" ) + ":" + path );

			try
			{
				Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
				fieldSysPath.setAccessible( true );
				fieldSysPath.set( null, null );
			}
			catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e )
			{
				PluginManager.getLogger().severe( "We could not force the ClassAppLoader to reinitalize the LD_LIBRARY_PATH variable. You may need to set '-Djava.library.path=" + baseDir.getAbsolutePath() + "' on next load because one or more dependencies may fail to load their native libraries.", e );
			}
		}

		return nativesExtracted.size() > 0;
	}

	public static boolean extractNatives( Map<String, List<String>> natives, File libFile, File baseDir ) throws IOException
	{
		if ( !UtilObjects.containsKeys( natives, Arrays.asList( OSInfo.NATIVE_SEARCH_PATHS ) ) )
			PluginManager.getLogger().warning( String.format( "%sWe were unable to locate any natives libraries that match architectures '%s' within plugin '%s'.", EnumColor.DARK_GRAY, Joiner.on( ", '" ).join( OSInfo.NATIVE_SEARCH_PATHS ), libFile.getAbsolutePath() ) );

		List<String> nativesExtracted = new ArrayList<>();
		baseDir = new File( baseDir, "natives" );
		// FileFunc.directoryHealthCheck( baseDir );

		if ( libFile == null || !libFile.exists() || !libFile.getName().endsWith( ".jar" ) )
			throw new IOException( "There was a problem with the provided jar file, it was either null, not existent or did not end with jar." );

		JarFile jar = new JarFile( libFile );

		for ( String arch : OSInfo.NATIVE_SEARCH_PATHS )
		{
			List<String> libs = natives.get( arch.toLowerCase() );
			if ( libs != null && !libs.isEmpty() )
				for ( String lib : libs )
					try
					{
						ZipEntry entry = jar.getEntry( lib );

						if ( entry == null || entry.isDirectory() )
						{
							entry = jar.getEntry( "natives/" + lib );

							if ( entry == null || entry.isDirectory() )
								PluginManager.getLogger().warning( String.format( "We were unable to load the native lib '%s' for arch '%s' for it was non-existent (or it's a directory) within plugin '%s'.", lib, arch, libFile ) );
						}

						if ( entry != null && !entry.isDirectory() )
						{
							if ( !entry.getName().endsWith( ".so" ) && !entry.getName().endsWith( ".dll" ) && !entry.getName().endsWith( ".jnilib" ) && !entry.getName().endsWith( ".dylib" ) )
								PluginManager.getLogger().warning( String.format( "We found native lib '%s' for arch '%s' within plugin '%s', but it did not end with a known native library ext. We will extract it anyways but you may have problems.", lib, arch, libFile ) );

							File newNative = new File( baseDir + "/" + arch + "/" + new File( entry.getName() ).getName() );

							if ( !newNative.exists() )
							{
								newNative.getParentFile().mkdirs();
								PluginManager.getLogger().info( String.format( "%sExtracting native library '%s' to '%s'.", EnumColor.GOLD, entry.getName(), newNative.getAbsolutePath() ) );
								InputStream is = jar.getInputStream( entry );
								FileOutputStream out = new FileOutputStream( newNative );
								ByteStreams.copy( is, out );
								is.close();
								out.close();
							}

							nativesExtracted.add( entry.getName() );
							// PluginManager.getLogger().severe( String.format( "We were unable to load the native lib '%s' for arch '%s' within plugin '%s' for an unknown reason.", lib, arch, libFile ) );
						}
					}
					catch ( FileNotFoundException e )
					{
						jar.close();
						throw new IOException( String.format( "We had a problem extracting native library '%s' from jar file '%s'", lib, libFile.getAbsolutePath() ), e );
					}
		}

		// Enumeration<JarEntry> entries = jar.entries();
		jar.close();

		if ( nativesExtracted.size() > 0 )
		{
			LibraryPath path = new LibraryPath();
			path.add( baseDir.getAbsolutePath() );
			for ( String arch : OSInfo.NATIVE_SEARCH_PATHS )
				path.add( baseDir.getAbsolutePath() + "/" + arch );
			path.set();

			try
			{
				Field fieldSysPath = ClassLoader.class.getDeclaredField( "sys_paths" );
				fieldSysPath.setAccessible( true );
				fieldSysPath.set( null, null );
			}
			catch ( NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e )
			{
				PluginManager.getLogger().severe( "We could not force the ClassAppLoader to reinitialize the LD_LIBRARY_PATH variable. You may need to set '-Djava.library.path=" + baseDir.getAbsolutePath() + "' on next load because one or more dependencies may fail to load their native libraries.", e );
			}
		}

		return nativesExtracted.size() > 0;
	}

	public static boolean extractZipResource( String path, File dest ) throws IOException
	{
		return extractZipResource( path, dest, AppLoader.class );
	}

	public static boolean extractZipResource( String path, File dest, Class<?> clz ) throws IOException
	{
		File cache = AppConfig.get().getDirectoryCache();
		if ( !cache.exists() )
			cache.mkdirs();
		File temp = new File( cache, "temp.zip" );
		putResource( clz, path, temp );

		ZipFile zip = new ZipFile( temp );

		try
		{
			Enumeration<? extends ZipEntry> entries = zip.entries();

			while ( entries.hasMoreElements() )
			{
				ZipEntry entry = entries.nextElement();
				File save = new File( dest, entry.getName() );
				if ( entry.isDirectory() )
					save.mkdirs();
				else if ( save.getParentFile() != null )
				{
					save.getParentFile().mkdirs();
					FileUtils.copyInputStreamToFile( zip.getInputStream( entry ), save );
				}
			}
		}
		finally
		{
			zip.close();
			temp.delete();
		}

		return true;
	}

	public static String fileExtension( File file )
	{
		return fileExtension( file.getName() );
	}

	private static String fileExtension( String fileName )
	{
		return UtilStrings.regexCapture( fileName, ".*\\.(.*)$" );
	}

	public static void gzFile( File source ) throws IOException
	{
		gzFile( source, new File( source + ".gz" ) );
	}

	public static void gzFile( File source, File dest ) throws IOException
	{
		byte[] buffer = new byte[1024];

		GZIPOutputStream gzos = new GZIPOutputStream( new FileOutputStream( dest ) );

		FileInputStream in = new FileInputStream( source );

		int len;
		while ( ( len = in.read( buffer ) ) > 0 )
			gzos.write( buffer, 0, len );

		in.close();

		gzos.finish();
		gzos.close();
	}

	public static ByteArrayOutputStream inputStream2ByteArray( InputStream is ) throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		int len;
		byte[] buffer = new byte[4096];
		while ( -1 != ( len = is.read( buffer ) ) )
			bos.write( buffer, 0, len );

		bos.flush();
		return bos;
	}

	public static byte[] inputStream2Bytes( InputStream is ) throws IOException
	{
		return inputStream2ByteArray( is ).toByteArray();
	}

	public static String inputStream2String( InputStream is ) throws IOException
	{
		return new String( inputStream2Bytes( is ) );
	}

	public static boolean isAbsolute( String dir )
	{
		return dir.startsWith( "/" ) || dir.startsWith( ":\\", 1 );
	}

	public static Map<String, File> mapExtensions( File[] files )
	{
		Map<String, File> maps = Maps.newTreeMap();
		for ( File f : files )
			maps.put( fileExtension( f ).toLowerCase(), f );
		return maps;
	}

	public static Map<Long, File> mapLastModified( File[] files )
	{
		Map<Long, File> maps = Maps.newTreeMap();
		for ( File f : files )
			maps.put( f.lastModified(), f );
		return maps;
	}

	public static String md5( File file )
	{
		return UtilEncryption.md5( file );
	}

	public static String nameSpaceToPath( String namespace )
	{
		return nameSpaceToPath( namespace, false );
	}

	public static String nameSpaceToPath( String namespace, boolean flip )
	{
		String output = "";

		if ( flip )
			for ( String s : namespace.split( "\\." ) )
				output = s + "." + output;

		return output.replaceAll( "\\.", PATH_SEPERATOR );
	}

	public static void putResource( Class<?> clz, String resource, File file ) throws IOException
	{
		try
		{
			InputStream is = clz.getClassLoader().getResourceAsStream( resource );
			if ( is == null )
				throw new IOException( String.format( "The resource %s does not exist.", resource ) );
			FileOutputStream os = new FileOutputStream( file );
			ByteStreams.copy( is, os );
			is.close();
			os.close();
		}
		catch ( FileNotFoundException e )
		{
			throw new IOException( e );
		}
	}

	public static void putResource( String resource, File file ) throws IOException
	{
		putResource( AppLoader.class, resource, file );
	}

	public static String readFileToString( File file ) throws IOException
	{
		InputStream in = null;
		try
		{
			in = new FileInputStream( file );

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ( ( nRead = in.read( data, 0, data.length ) ) != -1 )
				buffer.write( data, 0, nRead );

			return new String( buffer.toByteArray(), Charset.defaultCharset() );
		}
		finally
		{
			closeQuietly( in );
		}
	}

	public static List<String> readFileToLines( File file ) throws IOException
	{
		return new BufferedReader( new InputStreamReader( new FileInputStream( file ) ) ).lines().collect( Collectors.toList() );
	}

	public static List<File> recursiveFiles( final File dir )
	{
		return recursiveFiles( dir, 9999 );
	}

	private static List<File> recursiveFiles( final File start, final File current, final int depth, final int maxDepth, final String regexPattern )
	{
		final List<File> files = new ArrayList<>();

		current.list( new FilenameFilter()
		{
			@Override
			public boolean accept( File dir, String name )
			{
				dir = new File( dir, name );

				if ( dir.isDirectory() && depth < maxDepth )
					files.addAll( recursiveFiles( start, dir, depth + 1, maxDepth, regexPattern ) );

				if ( dir.isFile() )
				{
					String filename = dir.getAbsolutePath();
					filename = filename.substring( start.getAbsolutePath().length() + 1 );
					if ( regexPattern == null || filename.matches( regexPattern ) )
						files.add( dir );
				}

				return false;
			}
		} );

		return files;
	}

	public static List<File> recursiveFiles( final File dir, final int maxDepth )
	{
		return recursiveFiles( dir, maxDepth, null );
	}

	public static List<File> recursiveFiles( final File dir, final int maxDepth, final String regexPattern )
	{
		return recursiveFiles( dir, dir, 0, maxDepth, regexPattern );
	}

	/**
	 * Constructs a relative path from the server root
	 *
	 * @param file The file you wish to get relative to
	 * @return The relative path to the file, will return absolute if file is not relative to server root
	 */
	public static String relPath( File file )
	{
		return relPath( file, AppConfig.get().getDirectory() );
	}

	public static String relPath( File file, File relTo )
	{
		if ( file == null || relTo == null )
			return null;
		String root = relTo.getAbsolutePath();
		if ( file.getAbsolutePath().startsWith( root ) )
			return file.getAbsolutePath().substring( root.length() + 1 );
		else
			return file.getAbsolutePath();
	}

	public static String resourceToString( String resource ) throws IOException
	{
		return resourceToString( resource, AppController.class );
	}

	public static String resourceToString( String resource, Class<?> clz ) throws IOException
	{
		InputStream is = clz.getClassLoader().getResourceAsStream( resource );

		if ( is == null )
			return null;

		return new String( inputStream2Bytes( is ), "UTF-8" );
	}

	public static boolean setDirectoryAccess( File file )
	{
		if ( file.exists() && file.isDirectory() && file.canRead() && file.canWrite() )
			Log.get().finest( "This application has read and write access to directory \"" + relPath( file ) + "\"!" );
		else
			try
			{
				if ( file.exists() && file.isFile() )
					UtilObjects.notFalse( file.delete(), "failed to delete directory!" );
				UtilObjects.notFalse( file.mkdirs(), "failed to create directory!" );
				UtilObjects.notFalse( file.setWritable( true ), "failed to set directory writable!" );
				UtilObjects.notFalse( file.setReadable( true ), "failed to set directory readable!" );

				Log.get().fine( "Setting read and write access for directory \"" + relPath( file ) + "\" was successful!" );
			}
			catch ( IllegalArgumentException e )
			{
				Log.get().severe( "Exception encountered while handling access to path '" + relPath( file ) + "' with message '" + e.getMessage() + "'" );
				return false;
			}
		return true;
	}

	public static void setDirectoryAccessWithException( File file )
	{
		if ( !setDirectoryAccess( file ) )
			throw new UncaughtException( ReportingLevel.E_ERROR, "Experienced a problem setting read and write access to directory \"" + relPath( file ) + "\"!" );
	}

	public static void writeStringToFile( File file, String data ) throws IOException
	{
		writeStringToFile( file, data, false );
	}

	public static void writeStringToFile( File file, String data, boolean append ) throws IOException
	{
		BufferedWriter out = null;
		try
		{
			out = new BufferedWriter( new FileWriter( file, append ) );
			out.write( data );
		}
		finally
		{
			if ( out != null )
				out.close();
		}
	}

	public static void zipDir( File src, File dest ) throws IOException
	{
		if ( dest.isDirectory() )
			dest = new File( dest, "temp.zip" );

		ZipOutputStream out = new ZipOutputStream( new FileOutputStream( dest ) );
		try
		{
			zipDirRecursive( src, src, out );
		}
		finally
		{
			out.close();
		}
	}

	private static void zipDirRecursive( File origPath, File dirObj, ZipOutputStream out ) throws IOException
	{
		File[] files = dirObj.listFiles();
		byte[] tmpBuf = new byte[1024];

		for ( int i = 0; i < files.length; i++ )
		{
			if ( files[i].isDirectory() )
			{
				zipDirRecursive( origPath, files[i], out );
				continue;
			}
			FileInputStream in = new FileInputStream( files[i].getAbsolutePath() );
			out.putNextEntry( new ZipEntry( relPath( files[i], origPath ) ) );
			int len;
			while ( ( len = in.read( tmpBuf ) ) > 0 )
				out.write( tmpBuf, 0, len );
			out.closeEntry();
			in.close();
		}
	}

	public static boolean isDirectoryEmpty( File file )
	{
		UtilObjects.notNull( file, "file is null" );

		return file.exists() && file.isDirectory() && file.list().length == 0;
	}

	public static String getFileName( String path )
	{
		path = path.replace( "\\/", "/" );
		if ( path.contains( File.pathSeparator ) )
			return path.substring( path.lastIndexOf( File.pathSeparator ) + 1 );
		if ( path.contains( "/" ) )
			return path.substring( path.lastIndexOf( "/" ) + 1 );
		if ( path.contains( "\\" ) )
			return path.substring( path.lastIndexOf( "\\" ) + 1 );
		return path;
	}

	public static Byte[] bytesToBytes( byte[] bytes )
	{
		Byte[] newBytes = new Byte[bytes.length];
		for ( int i = 0; i < bytes.length; i++ )
			newBytes[i] = bytes[i];
		return newBytes;
	}

	static class LibraryPath
	{
		private List<String> libPath;

		LibraryPath()
		{
			read();
		}

		void add( String path )
		{
			if ( path.contains( " " ) )
				path = "\"" + path + "\"";
			if ( libPath.contains( path ) )
				return;
			libPath.add( path );
		}

		void read()
		{
			libPath = new ArrayList<>( Splitter.on( ":" ).splitToList( System.getProperty( "java.library.path" ) ) );
		}

		void set()
		{
			System.setProperty( "java.library.path", Joiner.on( ":" ).join( libPath ) );
		}
	}

	/**
	 * Separate class for native platform ID which is only loaded when native libs are loaded.
	 */
	public static class OSInfo
	{
		public static final String ARCH_NAME;
		public static final String CPU_ID;
		public static final String[] NATIVE_SEARCH_PATHS;
		public static final String OS_ID;

		static
		{
			final Object[] strings = AccessController.doPrivileged( new PrivilegedAction<Object[]>()
			{
				@Override
				public Object[] run()
				{
					// First, identify the operating system.
					boolean knownOs = true;
					String osName;
					// let the user override it.
					osName = System.getProperty( "chiori.os-name" );
					if ( osName == null )
					{
						String sysOs = System.getProperty( "os.name" );
						if ( sysOs == null )
						{
							osName = "unknown";
							knownOs = false;
						}
						else
						{
							sysOs = sysOs.toUpperCase( Locale.US );
							if ( sysOs.startsWith( "LINUX" ) )
								osName = "linux";
							else if ( sysOs.startsWith( "MAC OS" ) )
								osName = "macosx";
							else if ( sysOs.startsWith( "WINDOWS" ) )
								osName = "win";
							else if ( sysOs.startsWith( "OS/2" ) )
								osName = "os2";
							else if ( sysOs.startsWith( "SOLARIS" ) || sysOs.startsWith( "SUNOS" ) )
								osName = "solaris";
							else if ( sysOs.startsWith( "MPE/IX" ) )
								osName = "mpeix";
							else if ( sysOs.startsWith( "HP-UX" ) )
								osName = "hpux";
							else if ( sysOs.startsWith( "AIX" ) )
								osName = "aix";
							else if ( sysOs.startsWith( "OS/390" ) )
								osName = "os390";
							else if ( sysOs.startsWith( "OS/400" ) )
								osName = "os400";
							else if ( sysOs.startsWith( "FREEBSD" ) )
								osName = "freebsd";
							else if ( sysOs.startsWith( "OPENBSD" ) )
								osName = "openbsd";
							else if ( sysOs.startsWith( "NETBSD" ) )
								osName = "netbsd";
							else if ( sysOs.startsWith( "IRIX" ) )
								osName = "irix";
							else if ( sysOs.startsWith( "DIGITAL UNIX" ) )
								osName = "digitalunix";
							else if ( sysOs.startsWith( "OSF1" ) )
								osName = "osf1";
							else if ( sysOs.startsWith( "OPENVMS" ) )
								osName = "openvms";
							else if ( sysOs.startsWith( "IOS" ) )
								osName = "iOS";
							else
							{
								osName = "unknown";
								knownOs = false;
							}
						}
					}
					// Next, our CPU ID and its compatible variants.
					boolean knownCpu = true;
					ArrayList<String> cpuNames = new ArrayList<>();

					String cpuName = System.getProperty( "jboss.modules.cpu-name" );
					if ( cpuName == null )
					{
						String sysArch = System.getProperty( "os.arch" );
						if ( sysArch == null )
						{
							cpuName = "unknown";
							knownCpu = false;
						}
						else
						{
							boolean hasEndian = false;
							boolean hasHardFloatABI = false;
							sysArch = sysArch.toUpperCase( Locale.US );
							if ( sysArch.startsWith( "SPARCV9" ) || sysArch.startsWith( "SPARC64" ) )
								cpuName = "sparcv9";
							else if ( sysArch.startsWith( "SPARC" ) )
								cpuName = "sparc";
							else if ( sysArch.startsWith( "X86_64" ) || sysArch.startsWith( "AMD64" ) )
								cpuName = "x86_64";
							else if ( sysArch.startsWith( "I386" ) )
								cpuName = "i386";
							else if ( sysArch.startsWith( "I486" ) )
								cpuName = "i486";
							else if ( sysArch.startsWith( "I586" ) )
								cpuName = "i586";
							else if ( sysArch.startsWith( "I686" ) || sysArch.startsWith( "X86" ) || sysArch.contains( "IA32" ) )
								cpuName = "i686";
							else if ( sysArch.startsWith( "X32" ) )
								cpuName = "x32";
							else if ( sysArch.startsWith( "PPC64" ) )
								cpuName = "ppc64";
							else if ( sysArch.startsWith( "PPC" ) || sysArch.startsWith( "POWER" ) )
								cpuName = "ppc";
							else if ( sysArch.startsWith( "ARMV7A" ) || sysArch.contains( "AARCH32" ) )
							{
								hasEndian = true;
								hasHardFloatABI = true;
								cpuName = "armv7a";
							}
							else if ( sysArch.startsWith( "AARCH64" ) || sysArch.startsWith( "ARM64" ) || sysArch.startsWith( "ARMV8" ) || sysArch.startsWith( "PXA9" ) || sysArch.startsWith( "PXA10" ) )
							{
								hasEndian = true;
								cpuName = "aarch64";
							}
							else if ( sysArch.startsWith( "PXA27" ) )
							{
								hasEndian = true;
								cpuName = "armv5t-iwmmx";
							}
							else if ( sysArch.startsWith( "PXA3" ) )
							{
								hasEndian = true;
								cpuName = "armv5t-iwmmx2";
							}
							else if ( sysArch.startsWith( "ARMV4T" ) || sysArch.startsWith( "EP93" ) )
							{
								hasEndian = true;
								cpuName = "armv4t";
							}
							else if ( sysArch.startsWith( "ARMV4" ) || sysArch.startsWith( "EP73" ) )
							{
								hasEndian = true;
								cpuName = "armv4";
							}
							else if ( sysArch.startsWith( "ARMV5T" ) || sysArch.startsWith( "PXA" ) || sysArch.startsWith( "IXC" ) || sysArch.startsWith( "IOP" ) || sysArch.startsWith( "IXP" ) || sysArch.startsWith( "CE" ) )
							{
								hasEndian = true;
								String isaList = System.getProperty( "sun.arch.isalist" );
								if ( isaList != null )
								{
									if ( isaList.toUpperCase( Locale.US ).contains( "MMX2" ) )
										cpuName = "armv5t-iwmmx2";
									else if ( isaList.toUpperCase( Locale.US ).contains( "MMX" ) )
										cpuName = "armv5t-iwmmx";
									else
										cpuName = "armv5t";
								}
								else
									cpuName = "armv5t";
							}
							else if ( sysArch.startsWith( "ARMV5" ) )
							{
								hasEndian = true;
								cpuName = "armv5";
							}
							else if ( sysArch.startsWith( "ARMV6" ) )
							{
								hasEndian = true;
								hasHardFloatABI = true;
								cpuName = "armv6";
							}
							else if ( sysArch.startsWith( "PA_RISC2.0W" ) )
								cpuName = "parisc64";
							else if ( sysArch.startsWith( "PA_RISC" ) || sysArch.startsWith( "PA-RISC" ) )
								cpuName = "parisc";
							else if ( sysArch.startsWith( "IA64" ) )
								// HP-UX reports IA64W for 64-bit Itanium and IA64N when running
								// in 32-bit mode.
								cpuName = sysArch.toLowerCase( Locale.US );
							else if ( sysArch.startsWith( "ALPHA" ) )
								cpuName = "alpha";
							else if ( sysArch.startsWith( "MIPS" ) )
								cpuName = "mips";
							else
							{
								knownCpu = false;
								cpuName = "unknown";
							}

							boolean be = false;
							boolean hf = false;

							if ( knownCpu && hasEndian && "big".equals( System.getProperty( "sun.cpu.endian", "little" ) ) )
								be = true;

							if ( knownCpu && hasHardFloatABI )
							{
								String archAbi = System.getProperty( "sun.arch.abi" );
								if ( archAbi != null )
								{
									if ( archAbi.toUpperCase( Locale.US ).contains( "HF" ) )
										hf = true;
								}
								else
								{
									String libPath = System.getProperty( "java.library.path" );
									if ( libPath != null && libPath.toUpperCase( Locale.US ).contains( "GNUEABIHF" ) )
										hf = true;
								}
								if ( hf )
									cpuName += "-hf";
							}

							if ( knownCpu )
							{
								switch ( cpuName )
								{
									case "i686":
										cpuNames.add( "i686" );
									case "i586":
										cpuNames.add( "i586" );
									case "i486":
										cpuNames.add( "i486" );
									case "i386":
										cpuNames.add( "i386" );
										break;
									case "armv7a":
										cpuNames.add( "armv7a" );
										if ( hf )
											break;
									case "armv6":
										cpuNames.add( "armv6" );
										if ( hf )
											break;
									case "armv5t":
										cpuNames.add( "armv5t" );
									case "armv5":
										cpuNames.add( "armv5" );
									case "armv4t":
										cpuNames.add( "armv4t" );
									case "armv4":
										cpuNames.add( "armv4" );
										break;
									case "armv5t-iwmmx2":
										cpuNames.add( "armv5t-iwmmx2" );
									case "armv5t-iwmmx":
										cpuNames.add( "armv5t-iwmmx" );
										cpuNames.add( "armv5t" );
										cpuNames.add( "armv5" );
										cpuNames.add( "armv4t" );
										cpuNames.add( "armv4" );
										break;
									default:
										cpuNames.add( cpuName );
										break;
								}
								if ( hf || be )
									for ( int i = 0; i < cpuNames.size(); i++ )
									{
										String name = cpuNames.get( i );
										if ( be )
											name += "-be";
										if ( hf )
											name += "-hf";
										cpuNames.set( i, name );
									}
								cpuName = cpuNames.get( 0 );
							}
						}
					}

					// Finally, search paths.
					final int cpuCount = cpuNames.size();
					String[] searchPaths = new String[cpuCount];
					if ( knownOs && knownCpu )
						for ( int i = 0; i < cpuCount; i++ )
						{
							final String name = cpuNames.get( i );
							searchPaths[i] = osName + "-" + name;
						}
					else
						searchPaths = new String[0];

					return new Object[] {osName, cpuName, osName + "-" + cpuName, searchPaths};
				}
			} );
			OS_ID = strings[0].toString();
			CPU_ID = strings[1].toString();
			ARCH_NAME = strings[2].toString();
			NATIVE_SEARCH_PATHS = ( String[] ) strings[3];
		}
	}

	public static class SortableFile implements Comparable<SortableFile>
	{
		public long t;
		public File f;

		public SortableFile( File file )
		{
			f = file;
			t = file.lastModified();
		}

		@Override
		public int compareTo( SortableFile o )
		{
			long u = o.t;
			return t < u ? -1 : t == u ? 0 : 1;
		}
	}

	/**
	 * List directory contents for a resource folder. Not recursive.
	 * This is basically a brute-force implementation.
	 * Works for regular files and also JARs.
	 *
	 * @param clazz Any java class that lives in the same place as the resources you want.
	 * @param path  Should end with "/", but not start with one.
	 * @return Just the name of each member item, not the full paths.
	 * @throws URISyntaxException
	 * @throws IOException
	 * @author Greg Briggs
	 */
	public static String[] getResourceListing( Class<?> clazz, String path ) throws URISyntaxException, IOException
	{
		URL dirURL = clazz.getClassLoader().getResource( path );

		if ( dirURL == null )
		{
			/*
			 * In case of a jar file, we can't actually find a directory.
			 * Have to assume the same jar as clazz.
			 */
			String me = clazz.getName().replace( ".", "/" ) + ".class";
			dirURL = clazz.getClassLoader().getResource( me );
		}

		if ( dirURL.getProtocol().equals( "file" ) )
			/* A file path: easy enough */
			return new File( dirURL.toURI() ).list();

		if ( dirURL.getProtocol().equals( "jar" ) || dirURL.getProtocol().equals( "zip" ) )
		{
			/* A JAR or ZIP path */
			String archivePath = dirURL.getPath().substring( 5, dirURL.getPath().indexOf( "!" ) ); // strip out only the archive file
			Set<String> result;
			if ( dirURL.getProtocol().equals( "jar" ) )
			{
				JarFile jar = new JarFile( URLDecoder.decode( archivePath, "UTF-8" ) );
				result = entriesToSet( archivePath, jar.entries() );
				jar.close();
			}
			else// if ( dirURL.getProtocol().equals( "zip" ) )
			{
				ZipFile zip = new ZipFile( URLDecoder.decode( archivePath, "UTF-8" ) );
				result = entriesToSet( archivePath, zip.entries() );
				zip.close();
			}
			return result.toArray( new String[result.size()] );
		}

		throw new UnsupportedOperationException( "Cannot list files for URL " + dirURL );
	}

	public static Set<String> entriesToSet( String archivePath, Enumeration<? extends ZipEntry> entries )
	{
		Set<String> result = new HashSet<>(); // avoid duplicates in case it is a subdirectory
		while ( entries.hasMoreElements() )
		{
			String name = entries.nextElement().getName();
			if ( name.startsWith( archivePath ) )
			{ // filter according to the path
				String entry = name.substring( archivePath.length() );
				int checkSubdir = entry.indexOf( "/" );
				if ( checkSubdir >= 0 )
					// if it is a subdirectory, we just return the directory name
					entry = entry.substring( 0, checkSubdir );
				result.add( entry );
			}
		}
		return result;
	}
}
