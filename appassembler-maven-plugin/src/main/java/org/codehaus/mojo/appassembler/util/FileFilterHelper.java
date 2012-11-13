package org.codehaus.mojo.appassembler.util;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

public class FileFilterHelper {
    public static IOFileFilter makeDirectoryAware(IOFileFilter filter, String directoryName) {

	IOFileFilter directoryAwareFilter = FileFilterUtils.notFileFilter(
		FileFilterUtils.andFileFilter(
			FileFilterUtils.directoryFileFilter(), FileFilterUtils.nameFileFilter(directoryName)
		)
	);
	
	return FileFilterUtils.andFileFilter(filter, directoryAwareFilter);
    }
    
    public static IOFileFilter makeFileNameAware(IOFileFilter filter, String fileName) {
	IOFileFilter directoryAwareFilter = FileFilterUtils.notFileFilter(
		FileFilterUtils.andFileFilter(
			FileFilterUtils.fileFileFilter(), FileFilterUtils.nameFileFilter(fileName)
		)
	);
	
	return FileFilterUtils.andFileFilter(filter, directoryAwareFilter);
    }
    
    public static IOFileFilter makeSuffixAware(IOFileFilter filter, String suffixFileName) {
	IOFileFilter directoryAwareFilter = FileFilterUtils.notFileFilter(
		FileFilterUtils.andFileFilter(
			FileFilterUtils.fileFileFilter(), FileFilterUtils.suffixFileFilter(suffixFileName)
		)
	);
	
	return FileFilterUtils.andFileFilter(filter, directoryAwareFilter);
    }

    public static IOFileFilter makePatternFileNameAware(IOFileFilter filter, String pattern) {
	IOFileFilter directoryAwareFilter = FileFilterUtils.notFileFilter(
		FileFilterUtils.andFileFilter(
			FileFilterUtils.fileFileFilter(), new RegexFileFilter(pattern)
		)
	);
	
	return FileFilterUtils.andFileFilter(filter, directoryAwareFilter);
    }
    
    /**
     * This will create a FileFilter which is the same as in
     * plexus-utils (DirectoryScanner.DEFAULTEXCLUDES).
     * 
     * @return The initialized filter. 
     */
    public static IOFileFilter createDefaultFilter() {

	IOFileFilter filter = null;
	
	// CVS
	// "**/CVS", "**/CVS/**", 
	filter = FileFilterUtils.makeCVSAware(filter);
	
	// "**/.cvsignore",
	filter = makeFileNameAware(filter, ".cvsignore");

	// Subversion
	// "**/.svn", "**/.svn/**",
	filter = FileFilterUtils.makeSVNAware(filter);

	// RCS
	// "**/RCS", "**/RCS/**",
	filter = makeDirectoryAware(filter, "RCS");

	// SCCS
	// "**/SCCS", "**/SCCS/**",
	filter = makeDirectoryAware(filter, "SCCS");


	// "**/*~", "**/#*#", "**/.#*", "**/%*%", "**/._*",
	filter = makeSuffixAware(filter, "~");
	filter = makePatternFileNameAware(filter, "#.*#");
	filter = makePatternFileNameAware(filter, "%.*%");
	filter = makeSuffixAware(filter, ".#");
	filter = makeSuffixAware(filter, "._");


	
	// Visual SourceSafe
	// "**/vssver.scc",
	filter = makeFileNameAware(filter, "vssver.scc");

	// MKS
	// "**/project.pj",
	filter = makeFileNameAware(filter, "project.pj");

	// Arch
	// "**/.arch-ids", "**/.arch-ids/**",
	filter = makeDirectoryAware(filter, ".arch-ids");

	//Bazaar
	// "**/.bzr", "**/.bzr/**",
	filter = makeDirectoryAware(filter, ".bzr");

	//SurroundSCM
	// "**/.MySCMServerInfo",
	filter = makeFileNameAware(filter, ".MySCMServerInfo");

	// Mac
	// "**/.DS_Store",
	filter = makeDirectoryAware(filter, ".DS_Store");

	// Serena Dimensions Version 10
	// "**/.metadata", "**/.metadata/**",
	filter = makeDirectoryAware(filter, ".metadata");

	// Mercurial
	// "**/.hg", "**/.hg/**",
	filter = makeDirectoryAware(filter, ".hg");

	// git
	// "**/.git", "**/.gitignore", "**/.gitattributes", "**/.git/**",
	filter = makeDirectoryAware(filter, ".git");
	filter = makeFileNameAware(filter, ".gitignore");

	// BitKeeper
	//"**/BitKeeper", "**/BitKeeper/**", "**/ChangeSet", "**/ChangeSet/**",
	filter = makeDirectoryAware(filter, "BitKeeper");
	filter = makeDirectoryAware(filter, "ChangeSet");

	// darcs
	// "**/_darcs", "**/_darcs/**", "**/.darcsrepo", "**/.darcsrepo/**", "**/-darcs-backup*", "**/.darcs-temp-mail" };
	filter = makeDirectoryAware(filter, "_darcs");
	filter = makeDirectoryAware(filter, ".darcsrepo");

	return filter;
    }

}
